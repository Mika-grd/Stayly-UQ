package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.PoliticaCancelacionRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ReservaRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConsecutivoReservaJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.PoliticaCancelacionJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaJpaRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Reserva y PoliticaCancelacion van y vuelven de H2 sin perder estado, ocupantes ni historia. */
@DataJpaTest
class ReservaPoliticaJpaTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ReservaJpaRepository reservaJpa;
    @Autowired
    private ConsecutivoReservaJpaRepository consecutivoJpa;
    @Autowired
    private PoliticaCancelacionJpaRepository politicaJpa;

    private final Apartamento suq201 = FichaStaylyUq.suq201();
    private ReservaRepositoryJpa reservas;
    private PoliticaCancelacionRepositoryJpa politicas;

    @BeforeEach
    void crearAdaptadores() {
        reservas = new ReservaRepositoryJpa(reservaJpa, consecutivoJpa, relojEn(HOY));
        politicas = new PoliticaCancelacionRepositoryJpa(politicaJpa);
    }

    @Test
    @DisplayName("Reserva · Se guarda y se reconstituye con ocupantes, valor congelado, estado e historia")
    void guardarYLeerReservaConfirmada() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, diciembre(13, 15));

        // Act
        reservas.guardar(reserva);
        vaciarCache();
        Reserva leida = reservas.buscarPorCodigo(reserva.codigo()).orElseThrow();

        // Assert
        assertThat(leida).isEqualTo(reserva);
        assertThat(leida.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(leida.estancia()).isEqualTo(reserva.estancia());
        assertThat(leida.ocupantes()).containsExactlyElementsOf(reserva.ocupantes());
        Ocupante titular = leida.titular();
        assertThat(titular.documento()).isEqualTo(reserva.titular().documento());
        assertThat(titular.contacto()).isEqualTo(reserva.titular().contacto());
        assertThat(leida.valor()).isEqualTo(reserva.valor());
        assertThat(leida.horaEstimadaLlegada()).isEqualTo(reserva.horaEstimadaLlegada());
        assertThat(leida.versionPolitica()).isEqualTo(reserva.versionPolitica());
        assertThat(leida.eventos()).containsExactlyElementsOf(reserva.eventos());
    }

    @Test
    @DisplayName("Reserva · Guardar de nuevo tras una transición actualiza el estado y conserva los ocupantes")
    void actualizarReservaTrasUnaTransicion() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15));
        reservas.guardar(reserva);
        vaciarCache();

        // Act
        Reserva leida = reservas.buscarPorCodigo(reserva.codigo()).orElseThrow();
        leida.cancelar("huesped@correo.co", HOY.plusHours(1));
        reservas.guardar(leida);
        vaciarCache();

        // Assert
        Reserva cancelada = reservas.buscarPorCodigo(reserva.codigo()).orElseThrow();
        assertThat(cancelada.estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(cancelada.ocupantes()).hasSize(2);
        assertThat(cancelada.eventos()).hasSize(2);
    }

    @Test
    @DisplayName("Reserva · Solo las activas que comparten una noche cuentan como solapadas (RN-01, RN-12)")
    void buscarActivasQueSolapan() {
        // Arrange
        Reserva activa = FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15));
        Reserva cancelada = FichaStaylyUq.reservaPendiente(suq201, diciembre(14, 16));
        cancelada.cancelar("recepcion", HOY.plusHours(1));
        reservas.guardar(activa);
        reservas.guardar(cancelada);
        vaciarCache();

        // Act + Assert
        assertThat(reservas.buscarActivasQueSolapan(suq201.identificacion(), diciembre(14, 17)))
                .containsExactly(activa);
        assertThat(reservas.buscarActivasQueSolapan(suq201.identificacion(), diciembre(15, 17)))
                .as("la noche del 15 no es de la reserva del 13 al 15").isEmpty();
        assertThat(reservas.tieneActivasOFuturas(suq201.identificacion(), LocalDate.of(2026, 12, 1))).isTrue();
        assertThat(reservas.tieneActivasOFuturas(suq201.identificacion(), LocalDate.of(2026, 12, 15))).isFalse();
    }

    @Test
    @DisplayName("Reserva · Encuentra las PENDIENTE creadas antes de un límite (RN-21)")
    void buscarPendientesCreadasAntesDe() {
        // Arrange
        Reserva pendiente = FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15));
        reservas.guardar(pendiente);
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq201, diciembre(20, 22)));
        vaciarCache();

        // Act + Assert
        assertThat(reservas.buscarPendientesCreadasAntesDe(HOY.plusHours(25))).containsExactly(pendiente);
        assertThat(reservas.buscarPendientesCreadasAntesDe(HOY)).isEmpty();
    }

    @Test
    @DisplayName("Reserva · Un mismo identificador externo no se guarda dos veces (RN-19)")
    void identificadorExternoUnico() {
        // Arrange
        IdentificadorExterno externo = new IdentificadorExterno(new IdCanal(UUID.randomUUID()), "BK-7781");
        Reserva primera = FichaStaylyUq.reservaExterna(suq201, diciembre(13, 15), externo);
        reservas.guardar(primera);
        vaciarCache();

        // Act + Assert
        assertThat(reservas.buscarPorIdentificadorExterno(externo)).contains(primera);
        reservas.guardar(FichaStaylyUq.reservaExterna(suq201, diciembre(20, 22), externo));
        assertThatThrownBy(em::flush)
                .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    @DisplayName("Reserva · El código es RES-AAAA-NNNNN, consecutivo y reinicia cada año")
    void siguienteCodigoConsecutivoPorAnio() {
        // Act
        String primero = reservas.siguienteCodigo().valor();
        String segundo = reservas.siguienteCodigo().valor();
        String otroAnio = new ReservaRepositoryJpa(reservaJpa, consecutivoJpa,
                relojEn(LocalDateTime.of(2027, 1, 2, 9, 0))).siguienteCodigo().valor();

        // Assert
        assertThat(List.of(primero, segundo, otroAnio))
                .containsExactly("RES-2026-00001", "RES-2026-00002", "RES-2027-00001");
    }

    @Test
    @DisplayName("Reserva · El listado filtra en H2 por estado, titular y fechas, de la más reciente a la más antigua")
    void buscarConFiltrosEnH2() {
        // Arrange: tres reservas creadas en momentos distintos
        Reserva antigua = FichaStaylyUq.reservaPendiente(suq201, diciembre(1, 3));
        Reserva media = reservaCreadaEn(diciembre(5, 7), HOY.plusHours(1));
        Reserva reciente = reservaCreadaEn(diciembre(10, 12), HOY.plusHours(2));
        reciente.cancelar("recepcion", HOY.plusHours(3));
        reservas.guardar(antigua);
        reservas.guardar(media);
        reservas.guardar(reciente);
        vaciarCache();

        // Act + Assert
        assertThat(reservas.buscar(FiltroReservas.ninguno(), PeticionPagina.primera()).contenido())
                .containsExactly(reciente, media, antigua);
        assertThat(reservas.buscar(new FiltroReservas(EstadoReserva.CANCELADA, null, null, null, null, null),
                PeticionPagina.primera()).contenido()).containsExactly(reciente);
        assertThat(reservas.buscar(new FiltroReservas(null, null, suq201.identificacion(), "LAURA", null, null),
                PeticionPagina.primera()).totalElementos()).as("titular sin distinguir mayúsculas").isEqualTo(3);
        assertThat(reservas.buscar(new FiltroReservas(null, null, null, "nadie", null, null),
                PeticionPagina.primera()).contenido()).isEmpty();
        assertThat(reservas.buscar(new FiltroReservas(null, CanalOrigen.PORTAL, null, null,
                LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 8)), PeticionPagina.primera()).contenido())
                .as("solo la que comparte noches con [4-dic, 8-dic)").containsExactly(media);
    }

    @Test
    @DisplayName("Política · La vigente es la de mayor versión y las anteriores se conservan (F-07, RN-13)")
    void versionesDePolitica() {
        // Arrange
        PoliticaCancelacion v1 = FichaStaylyUq.politicaV1();
        PoliticaCancelacion v2 = v1.nuevaVersion(List.of(new TramoPolitica(30, 0), new TramoPolitica(0, 50)), 40,
                LocalDateTime.of(2026, 10, 15, 0, 0));
        politicas.guardar(v1);
        politicas.guardar(v2);
        vaciarCache();

        // Act
        PoliticaCancelacion vigente = politicas.buscarVigente();
        PoliticaCancelacion anterior = politicas.buscarPorVersion(new VersionPolitica(1));

        // Assert
        assertThat(vigente.version()).isEqualTo(new VersionPolitica(2));
        assertThat(vigente.porcentajeRetencionNoShow()).isEqualTo(40);
        assertThat(anterior.tramos()).containsExactlyElementsOf(v1.tramos());
        assertThatThrownBy(() -> politicas.buscarPorVersion(new VersionPolitica(9)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("POLITICA_NO_ENCONTRADA");
    }

    /** Reserva PENDIENTE del portal creada en {@code momento}, con un titular y un adulto. */
    private Reserva reservaCreadaEn(Estancia estancia, LocalDateTime momento) {
        return Reserva.crear(FichaStaylyUq.siguienteCodigo(), suq201.identificacion(), estancia,
                List.of(FichaStaylyUq.titularAdulto(), FichaStaylyUq.adulto()), CanalOrigen.PORTAL, null, null,
                MascotasAutorizadas.ninguna(), FichaStaylyUq.valorFijo(estancia, 75_000, 2), new VersionPolitica(1),
                FichaStaylyUq.UMBRAL, "huesped@correo.co", momento);
    }

    /** Obliga a leer de H2 y no de la caché de primer nivel de JPA. */
    private void vaciarCache() {
        em.flush();
        em.clear();
    }

    private static Clock relojEn(LocalDateTime momento) {
        return Clock.fixed(momento.atZone(BOGOTA).toInstant(), BOGOTA);
    }
}

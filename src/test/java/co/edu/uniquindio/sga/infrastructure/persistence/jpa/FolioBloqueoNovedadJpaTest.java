package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.folio.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.EstadoFolio;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.IdNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.BloqueoRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.FolioRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.NovedadRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.BloqueoJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.FolioJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.NovedadJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Folio, Bloqueo y Novedad van y vuelven de H2 sin perder movimientos, estado ni historial. */
@DataJpaTest
class FolioBloqueoNovedadJpaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 12, 17, 11, 30);
    private static final IdentificacionApartamento SUQ_201 = new IdentificacionApartamento("SUQ-201");

    @Autowired
    private TestEntityManager em;
    @Autowired
    private FolioJpaRepository folioJpa;
    @Autowired
    private BloqueoJpaRepository bloqueoJpa;
    @Autowired
    private NovedadJpaRepository novedadJpa;

    private FolioRepositoryJpa folios;
    private BloqueoRepositoryJpa bloqueos;
    private NovedadRepositoryJpa novedades;

    @BeforeEach
    void crearAdaptadores() {
        folios = new FolioRepositoryJpa(folioJpa);
        bloqueos = new BloqueoRepositoryJpa(bloqueoJpa);
        novedades = new NovedadRepositoryJpa(novedadJpa);
    }

    @Test
    @DisplayName("Folio · Conserva cargos, movimientos inversos, pagos de varios medios y el saldo calculado")
    void guardarYLeerFolioAbierto() {
        // Arrange
        CodigoReserva reserva = new CodigoReserva("RES-2026-00042");
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva, Dinero.pesos(488_000), AHORA);
        Cargo servicio = new Cargo(TipoCargo.SERVICIO_ADICIONAL, "Mascota", Dinero.pesos(50_000), AHORA);
        folio.registrarCargo(servicio);
        folio.registrarCargo(servicio.inverso(AHORA.plusHours(1)));
        folio.registrarPago(new Pago(Dinero.pesos(146_400), MedioPago.TRANSFERENCIA, AHORA, "TRF-001"));
        folio.registrarPago(new Pago(Dinero.pesos(100_000), MedioPago.EFECTIVO, AHORA, null));

        // Act
        folios.guardar(folio);
        vaciarCache();
        Folio leido = folios.buscarPorCodigoReserva(reserva).orElseThrow();

        // Assert
        assertThat(leido).isEqualTo(folio);
        assertThat(leido.cargos()).containsExactlyElementsOf(folio.cargos());
        assertThat(leido.pagos()).containsExactlyElementsOf(folio.pagos());
        assertThat(leido.saldo().valor()).as("488.000 + 50.000 − 50.000 − 146.400 − 100.000")
                .isEqualTo(Dinero.pesos(241_600));
        assertThat(leido.estado()).isEqualTo(EstadoFolio.ABIERTO);
    }

    @Test
    @DisplayName("Folio · Un folio cerrado con saldo conserva la autorización registrada (RN-17)")
    void guardarYLeerFolioCerradoConAutorizacion() {
        // Arrange
        CodigoReserva reserva = new CodigoReserva("RES-2026-00043");
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva, Dinero.pesos(488_000), AHORA);
        AutorizacionCierre autorizacion = new AutorizacionCierre("admin@stayly-uq.co",
                "Huésped sin fondos; se gestiona el cobro por fuera", AHORA);
        folio.cerrarConAutorizacion(autorizacion);

        // Act
        folios.guardar(folio);
        vaciarCache();
        Folio leido = folios.buscarPorCodigoReserva(reserva).orElseThrow();

        // Assert
        assertThat(leido.estaCerrado()).isTrue();
        assertThat(leido.autorizacion()).contains(autorizacion);
        assertThat(leido.saldo().valor()).isEqualTo(Dinero.pesos(488_000));
    }

    @Test
    @DisplayName("Bloqueo · Solo los vigentes del apartamento que comparten una noche impiden vender (RN-07)")
    void buscarBloqueosVigentesQueSolapan() {
        // Arrange
        Bloqueo vigente = bloqueo(SUQ_201, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), false);
        Bloqueo levantado = bloqueo(SUQ_201, LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 8), true);
        Bloqueo otroApartamento = bloqueo(new IdentificacionApartamento("SUQ-102"),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10), false);
        bloqueos.guardar(vigente);
        bloqueos.guardar(levantado);
        bloqueos.guardar(otroApartamento);
        vaciarCache();

        // Act + Assert
        assertThat(bloqueos.buscarVigentesQueSolapan(SUQ_201,
                new Estancia(LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 7)))).containsExactly(vigente);
        assertThat(bloqueos.buscarVigentesQueSolapan(SUQ_201,
                new Estancia(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7))))
                .as("el bloqueo [1-dic, 5-dic) no incluye la noche del 5").isEmpty();
    }

    @Test
    @DisplayName("Novedad · El historial de un apartamento sale ordenado por fecha y sin las de otros")
    void listarNovedadesPorApartamento() {
        // Arrange
        Novedad tarde = novedad(SUQ_201, "Bombillo fundido en la cocina", GravedadNovedad.BAJA, AHORA.plusHours(5));
        Novedad temprano = novedad(SUQ_201, "Fuga en el lavamanos", GravedadNovedad.ALTA, AHORA);
        novedades.guardar(tarde);
        novedades.guardar(temprano);
        novedades.guardar(novedad(new IdentificacionApartamento("SUQ-102"), "Silla rota", GravedadNovedad.MEDIA, AHORA));
        vaciarCache();

        // Act + Assert
        assertThat(novedades.listarPorApartamento(SUQ_201)).containsExactly(temprano, tarde);
        assertThat(novedades.listarPorApartamento(SUQ_201).get(0).gravedad()).isEqualTo(GravedadNovedad.ALTA);
    }

    private static Bloqueo bloqueo(IdentificacionApartamento apartamento, LocalDate inicio, LocalDate fin,
                                   boolean levantado) {
        Bloqueo bloqueo = Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), apartamento, new RangoFechas(inicio, fin),
                "Mantenimiento", "admin@stayly-uq.co", AHORA);
        if (levantado) {
            bloqueo.levantar();
        }
        return bloqueo;
    }

    private static Novedad novedad(IdentificacionApartamento apartamento, String descripcion, GravedadNovedad gravedad,
                                   LocalDateTime fecha) {
        return Novedad.registrar(new IdNovedad(UUID.randomUUID()), apartamento, "servicio@stayly-uq.co", descripcion,
                gravedad, fecha);
    }

    /** Obliga a leer de H2 y no de la caché de primer nivel de JPA. */
    private void vaciarCache() {
        em.flush();
        em.clear();
    }
}

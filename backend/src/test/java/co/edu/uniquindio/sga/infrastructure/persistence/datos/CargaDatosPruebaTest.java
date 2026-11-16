package co.edu.uniquindio.sga.infrastructure.persistence.datos;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/** Sección 12.3: al arrancar, la base queda con los datos suficientes para operar y demostrar. */
@SpringBootTest
class CargaDatosPruebaTest {

    @Autowired
    private ApartamentoRepository apartamentos;
    @Autowired
    private ReservaRepository reservas;
    @Autowired
    private FolioRepository folios;
    @Autowired
    private BloqueoRepository bloqueos;
    @Autowired
    private ReservaJpaRepository reservaJpa;
    @Autowired
    private VerificadorTarifasCompletasService verificadorTarifas;
    @Autowired
    private Clock reloj;

    @Test
    @DisplayName("12.3 · Los 6 apartamentos de la Ficha están activos y con tarifa en todas las temporadas")
    void apartamentosConTarifasCompletas() {
        List<String> ficha = List.of("SUQ-101", "SUQ-102", "SUQ-201", "SUQ-202", "SUQ-301", "SUQ-302");

        assertThat(ficha).allSatisfy(id -> {
            Apartamento apartamento = apartamentos.buscarPorIdentificacion(new IdentificacionApartamento(id))
                    .orElseThrow();
            assertThat(apartamento.estaActivo()).isTrue();
            assertThatCode(() -> verificadorTarifas.verificarTarifasCompletas(apartamento.identificacion()))
                    .doesNotThrowAnyException();
        });
    }

    @Test
    @DisplayName("12.3 · Hay reservas en los 6 estados del ciclo de vida y por los 3 canales, cada una con su folio")
    void reservasEnTodosLosEstadosYCanales() {
        // Act
        DatosCargados reservasPorEstado = cargarPorEstado();

        // Assert
        assertThat(reservasPorEstado.porEstado().keySet()).containsExactlyInAnyOrder(EstadoReserva.values());
        assertThat(reservasPorEstado.canales()).containsExactlyInAnyOrder(CanalOrigen.values());
        assertThat(reservasPorEstado.porEstado().values())
                .allSatisfy(reserva -> assertThat(folios.buscarPorCodigoReserva(reserva.codigo())).isPresent());
    }

    @Test
    @DisplayName("12.3 · Cada reserva quedó en un estado coherente con su apartamento y su folio")
    void estadosCoherentesConApartamentoYFolio() {
        // Arrange
        DatosCargados datos = cargarPorEstado();
        Reserva enCurso = datos.porEstado().get(EstadoReserva.EN_CURSO);
        Reserva finalizada = datos.porEstado().get(EstadoReserva.FINALIZADA);
        Reserva noShow = datos.porEstado().get(EstadoReserva.NO_SHOW);
        Reserva confirmada = datos.porEstado().get(EstadoReserva.CONFIRMADA);

        // Assert
        assertThat(apartamento(enCurso).estadoOperativo()).isEqualTo(EstadoOperativo.OCUPADO);
        assertThat(apartamento(finalizada).estadoOperativo()).isEqualTo(EstadoOperativo.PENDIENTE_PREPARACION);
        Folio folioFinalizada = folios.buscarPorCodigoReserva(finalizada.codigo()).orElseThrow();
        assertThat(folioFinalizada.estaCerrado()).isTrue();
        assertThat(folioFinalizada.saldo().esCero()).isTrue();
        assertThat(folios.buscarPorCodigoReserva(noShow.codigo()).orElseThrow().saldo().esCero())
                .as("la retención del no-show equivale al anticipo pagado").isTrue();
        assertThat(confirmada.horaEstimadaLlegada()).isPresent();
        assertThat(enCurso.identificadorExterno()).isPresent();
        assertThat(enCurso.mascotas().hayMascotas()).isTrue();
    }

    @Test
    @DisplayName("12.3 · SUQ-302 está fuera de servicio y tiene un bloqueo vigente que impide venderlo (3.3)")
    void apartamentoFueraDeServicioConBloqueo() {
        // Arrange
        IdentificacionApartamento suq302 = new IdentificacionApartamento("SUQ-302");
        LocalDate hoy = LocalDate.now(reloj);

        // Act + Assert
        assertThat(apartamentos.buscarPorIdentificacion(suq302).orElseThrow().estadoOperativo())
                .isEqualTo(EstadoOperativo.FUERA_DE_SERVICIO);
        assertThat(bloqueos.buscarVigentesQueSolapan(suq302, new Estancia(hoy, hoy.plusDays(2)))).isNotEmpty();
    }

    private Apartamento apartamento(Reserva reserva) {
        return apartamentos.buscarPorIdentificacion(reserva.apartamento()).orElseThrow();
    }

    /** Una reserva de la carga por cada estado y los canales que aparecen. */
    private DatosCargados cargarPorEstado() {
        List<Reserva> todas = reservaJpa.findAll().stream()
                .map(ReservaEntity::getCodigo)
                .map(codigo -> reservas.buscarPorCodigo(new CodigoReserva(codigo)))
                .map(Optional::orElseThrow)
                .toList();
        Map<EstadoReserva, Reserva> porEstado = todas.stream()
                .collect(Collectors.toMap(Reserva::estado, Function.identity(), (primera, otra) -> primera));
        return new DatosCargados(porEstado, todas.stream().map(Reserva::canal).collect(Collectors.toSet()));
    }

    private record DatosCargados(Map<EstadoReserva, Reserva> porEstado, Set<CanalOrigen> canales) {
    }
}

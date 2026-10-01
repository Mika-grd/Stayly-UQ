package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorEstanciaMinimaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.PoliticaCancelacionRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** CU-02 a CU-05 de punta a punta con repositorios en memoria y un reloj fijo. */
class ReservaUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final ConfiguracionAlojamientoFake configuracion = ConfiguracionAlojamientoFake.ficha();
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    private final TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
    private final TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
    private final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria();
    private Clock reloj = relojEn(LocalDateTime.of(2026, 10, 1, 10, 0));
    private ReservaRepositoryEnMemoria reservas;
    private Apartamento suq301;

    @BeforeEach
    void cargarFicha() {
        reservas = new ReservaRepositoryEnMemoria(reloj);
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        tarifarios.guardar(FichaStaylyUq.tarifario("SUQ-301", 65_000, 88_000, 75_000));
        politicas.guardar(FichaStaylyUq.politicaV1());
        suq301 = FichaStaylyUq.suq301();
        apartamentos.guardar(suq301);
    }

    @Test
    @DisplayName("CU-02 · Crea la reserva PENDIENTE con folio, cargo por mascotas, recargo nocturno y advertencia")
    void crearReservaConReglasPropias() {
        // Arrange
        CrearReservaUseCase.Comando comando = comandoSuq301(LocalTime.of(21, 30), 1);

        // Act
        CrearReservaUseCase.ReservaCreada creada = crearReserva().ejecutar(comando);

        // Assert
        Reserva reserva = creada.reserva();
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.PENDIENTE);
        assertThat(reserva.codigo().valor()).isEqualTo("RES-2026-00001");
        assertThat(reserva.valor().total()).as("2 noches base × 3 facturables × $65.000").isEqualTo(Dinero.pesos(390_000));
        assertThat(creada.folio().cargos()).extracting(Cargo::valor)
                .containsExactly(Dinero.pesos(390_000), Dinero.pesos(50_000), Dinero.pesos(30_000));
        assertThat(creada.advertenciaAcceso()).contains("Tercer piso sin ascensor");
        assertThat(reservas.buscarPorCodigo(reserva.codigo())).isPresent();
        assertThat(folios.buscarPorCodigoReserva(reserva.codigo())).isPresent();
    }

    @Test
    @DisplayName("CU-03 y CU-05 · Confirma con anticipo y registra la llegada el día de entrada")
    void confirmarYRegistrarLlegada() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        Folio folio = folios.buscarPorCodigoReserva(reserva.codigo()).orElseThrow();
        folio.registrarPago(new Pago(Dinero.pesos(117_000), MedioPago.TRANSFERENCIA,
                LocalDateTime.of(2026, 10, 1, 12, 0), "TRF-100"));

        // Act
        new ConfirmarReservaUseCase(reservas, folios, new ConfirmacionReservaService(configuracion), reloj)
                .ejecutar(reserva.codigo(), "recepcion");
        reloj = relojEn(LocalDateTime.of(2026, 11, 20, 15, 0));
        new RegistrarLlegadaUseCase(reservas, apartamentos, new EntregaApartamentoService(), reloj)
                .ejecutar(reserva.codigo(), "recepcion");

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.EN_CURSO);
        assertThat(suq301.estadoOperativo()).isEqualTo(EstadoOperativo.OCUPADO);
    }

    @Test
    @DisplayName("CU-04 · Cancela con 10 días de antelación: retiene el 30 % y anula el alojamiento")
    void cancelarReserva() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        reloj = relojEn(LocalDateTime.of(2026, 11, 10, 9, 0));

        // Act
        Folio folio = new CancelarReservaUseCase(reservas, folios, new CalculadorRetencionService(politicas), reloj)
                .ejecutar(reserva.codigo(), "huesped");

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(117_000));
        assertThatThrownBy(() -> new CancelarReservaUseCase(reservas, folios,
                new CalculadorRetencionService(politicas), reloj).ejecutar(reserva.codigo(), "huesped"))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TRANSICION_INVALIDA");
    }

    private CrearReservaUseCase crearReserva() {
        DisponibilidadApartamentoService disponibilidad =
                new DisponibilidadApartamentoService(reservas, new BloqueoRepositoryEnMemoria(), configuracion);
        return new CrearReservaUseCase(apartamentos, reservas, folios, politicas,
                new VerificadorTarifasCompletasService(temporadas, tarifarios),
                new VerificadorEstanciaMinimaService(temporadas), new AdmisionMascotasService(configuracion),
                disponibilidad, new CotizadorEstanciaService(temporadas, tarifarios, configuracion), configuracion,
                reloj);
    }

    /** SUQ-301 del 20 al 22/11/2026 (temporada base) con dos adultos y un niño de 13 años. */
    private CrearReservaUseCase.Comando comandoSuq301(LocalTime horaLlegada, int mascotas) {
        return new CrearReservaUseCase.Comando(suq301.identificacion(), LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 22), List.of(
                new CrearReservaUseCase.DatosOcupante("Laura Gómez", LocalDate.of(1990, 5, 10), "1094000001", true,
                        "laura@correo.co", "3001234567"),
                new CrearReservaUseCase.DatosOcupante("Andrés Ríos", LocalDate.of(1988, 2, 1), null, false, null, null),
                new CrearReservaUseCase.DatosOcupante("Sara Ríos", LocalDate.of(2013, 4, 2), null, false, null, null)),
                CanalOrigen.PORTAL, null, horaLlegada, mascotas, "laura@correo.co");
    }

    private static Clock relojEn(LocalDateTime momento) {
        return Clock.fixed(momento.atZone(BOGOTA).toInstant(), BOGOTA);
    }
}

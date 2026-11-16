package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Reportes con repositorios en memoria y cifras calculadas a mano. */
class ReporteUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final LocalDate DIC_1 = LocalDate.of(2026, 12, 1);

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria(reloj);
    private final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    private Apartamento suq201;
    private Apartamento suq102;

    @BeforeEach
    void cargar() {
        suq201 = FichaStaylyUq.suq201();
        suq102 = FichaStaylyUq.suq102();
        apartamentos.guardar(suq201);
        apartamentos.guardar(suq102);

        // Vendidas: 1-4 dic en SUQ-201 (3 noches) y 3-6 dic en SUQ-102 (3 noches)
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq201, FichaStaylyUq.diciembre(1, 4)));
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq102, FichaStaylyUq.diciembre(3, 6)));
        // No cuentan: una PENDIENTE y una CANCELADA
        reservas.guardar(FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(10, 12)));
        Reserva cancelada = FichaStaylyUq.reservaPendiente(suq102, FichaStaylyUq.diciembre(1, 2));
        cancelada.cancelar("huesped@correo.co", HOY.plusHours(2));
        reservas.guardar(cancelada);
    }

    @Test
    @DisplayName("Ocupación · [1, 5) dic: 3 + 2 noches vendidas de 2 apartamentos × 4 noches = 0,625")
    void ocupacion() {
        var reporte = new GenerarReporteOcupacionUseCase(apartamentos, reservas).ejecutar(DIC_1, DIC_1.plusDays(4));

        assertThat(reporte.apartamentosActivos()).isEqualTo(2);
        assertThat(reporte.nochesPeriodo()).isEqualTo(4);
        assertThat(reporte.nochesVendidas()).isEqualTo(5);
        assertThat(reporte.porcentajeOcupacion()).isEqualTo(0.625);
    }

    @Test
    @DisplayName("Ocupación · Un apartamento inactivo no suma noches disponibles ni vendidas")
    void ocupacionSinInactivos() {
        suq102.retirar();
        apartamentos.guardar(suq102);

        var reporte = new GenerarReporteOcupacionUseCase(apartamentos, reservas).ejecutar(DIC_1, DIC_1.plusDays(4));

        assertThat(reporte.apartamentosActivos()).isEqualTo(1);
        assertThat(reporte.nochesVendidas()).isEqualTo(3);
        assertThat(reporte.porcentajeOcupacion()).isEqualTo(0.75);
    }

    @Test
    @DisplayName("Ingresos · Suma los pagos del rango por medio; las correcciones restan y lo de fuera no cuenta")
    void ingresos() {
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), FichaStaylyUq.siguienteCodigo(),
                Dinero.pesos(500_000), HOY);
        folio.registrarPago(new Pago(Dinero.pesos(150_000), MedioPago.TRANSFERENCIA, LocalDateTime.of(2026, 10, 1, 9, 0), null));
        folio.registrarPago(new Pago(Dinero.pesos(200_000), MedioPago.EFECTIVO, LocalDateTime.of(2026, 10, 2, 9, 0), null));
        folio.registrarPago(new Pago(Dinero.pesos(-20_000), MedioPago.EFECTIVO, LocalDateTime.of(2026, 10, 2, 9, 5), "Reverso"));
        folio.registrarPago(new Pago(Dinero.pesos(90_000), MedioPago.TARJETA, LocalDateTime.of(2026, 10, 3, 0, 0), null));
        folios.guardar(folio);

        var reporte = new GenerarReporteIngresosUseCase(folios)
                .ejecutar(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));

        assertThat(reporte.total()).isEqualTo(Dinero.pesos(330_000));
        assertThat(reporte.porMedioPago()).extracting(GenerarReporteIngresosUseCase.IngresoPorMedio::total)
                .containsExactly(Dinero.pesos(180_000), Dinero.pesos(150_000), Dinero.cero());
    }

    @Test
    @DisplayName("Por canal · Cuenta las reservas creadas en el rango por canal, con sus cancelaciones")
    void reservasPorCanal() {
        reservas.guardar(FichaStaylyUq.reservaExterna(suq201, FichaStaylyUq.diciembre(20, 22),
                new IdentificadorExterno(new IdCanal(UUID.randomUUID()), "BKG-1")));

        var reporte = new GenerarReporteReservasPorCanalUseCase(reservas)
                .ejecutar(HOY.toLocalDate(), HOY.toLocalDate().plusDays(1));

        assertThat(reporte.porCanal()).extracting(GenerarReporteReservasPorCanalUseCase.ReservasCanal::canal)
                .containsExactly(CanalOrigen.values());
        assertThat(reporte.porCanal().getFirst()).as("PORTAL")
                .isEqualTo(new GenerarReporteReservasPorCanalUseCase.ReservasCanal(CanalOrigen.PORTAL, 4, 1, 0));
        assertThat(reporte.porCanal().getLast()).as("EXTERNO")
                .isEqualTo(new GenerarReporteReservasPorCanalUseCase.ReservasCanal(CanalOrigen.EXTERNO, 1, 0, 0));
    }

    @Test
    @DisplayName("3.1 · Hasta igual o anterior a desde es RANGO_FECHAS_INVALIDO")
    void rangoInvalido() {
        assertThatThrownBy(() -> new GenerarReporteIngresosUseCase(folios).ejecutar(DIC_1, DIC_1))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "RANGO_FECHAS_INVALIDO");
    }
}

package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FolioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 12, 17, 11, 30);

    /** SUQ-102, 2 noches de temporada alta, 2 facturables: 2 × 2 × $122.000 = $488.000. */
    private Folio folioDeReserva() {
        return Folio.abrir(new IdFolio(UUID.randomUUID()), new CodigoReserva("RES-2026-00042"),
                Dinero.pesos(488_000), LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    @DisplayName("PU-08 · No cierra un folio con saldo sin autorización (RN-17)")
    void noCerrarConSaldoSinAutorizacion() {
        // Arrange
        Folio folio = folioDeReserva();
        folio.registrarPago(new Pago(Dinero.pesos(341_600), MedioPago.TRANSFERENCIA, AHORA, "TRF-001"));
        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(146_400));

        // Act + Assert
        assertThatThrownBy(() -> folio.cerrar(AHORA))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("FOLIO_CON_SALDO_SIN_AUTORIZACION");
        assertThat(folio.estaCerrado()).isFalse();

        // Act
        folio.cerrarConAutorizacion(new AutorizacionCierre("admin@stayly-uq.co",
                "Huésped sin fondos; se gestiona el cobro por fuera", AHORA));

        // Assert
        assertThat(folio.estaCerrado()).isTrue();
        assertThat(folio.autorizacion()).isPresent();
    }

    // Pruebas adicionales de la Matriz de Trazabilidad

    @Test
    @DisplayName("RN-15 · El saldo es la suma de cargos menos la suma de pagos")
    void saldoEsCargosMenosPagos() {
        // Arrange
        Folio folio = folioDeReserva();
        folio.registrarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL, "Cargo por mascotas", Dinero.pesos(50_000), AHORA));
        folio.registrarPago(new Pago(Dinero.pesos(146_400), MedioPago.TRANSFERENCIA, AHORA, "TRF-001"));
        folio.registrarPago(new Pago(Dinero.pesos(100_000), MedioPago.EFECTIVO, AHORA, null));

        // Act
        Saldo saldo = folio.saldo();

        // Assert
        assertThat(folio.totalCargos()).isEqualTo(Dinero.pesos(538_000));
        assertThat(folio.totalPagos()).isEqualTo(Dinero.pesos(246_400));
        assertThat(saldo.valor()).isEqualTo(Dinero.pesos(291_600));
        assertThat(saldo.debeElHuesped()).isTrue();
    }

    @Test
    @DisplayName("RN-16 · La lista de cargos no se puede modificar desde fuera del folio")
    void noPermitirModificarListaDeCargos() {
        // Arrange
        Folio folio = folioDeReserva();

        // Act + Assert
        assertThatThrownBy(() -> folio.cargos().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> folio.pagos().add(new Pago(Dinero.pesos(1_000), MedioPago.EFECTIVO, AHORA, null)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(folio.cargos()).hasSize(1);
    }

    @Test
    @DisplayName("RN-16 · Un pago mal registrado se corrige con un movimiento inverso")
    void correccionConMovimientoInverso() {
        // Arrange
        Folio folio = folioDeReserva();
        Pago pagoErrado = new Pago(Dinero.pesos(400_000), MedioPago.TARJETA, AHORA, "DAT-77");
        folio.registrarPago(pagoErrado);

        // Act
        folio.registrarPago(pagoErrado.inverso(AHORA.plusMinutes(5)));
        folio.registrarPago(new Pago(Dinero.pesos(40_000), MedioPago.TARJETA, AHORA.plusMinutes(6), "DAT-78"));

        // Assert
        assertThat(folio.pagos()).hasSize(3);
        assertThat(folio.pagos().get(0)).isEqualTo(pagoErrado);
        assertThat(folio.totalPagos()).isEqualTo(Dinero.pesos(40_000));
        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(448_000));
    }

    @Test
    @DisplayName("F-5 · Un folio cerrado no admite nuevos movimientos")
    void noRegistrarMovimientosEnFolioCerrado() {
        // Arrange
        Folio folio = folioDeReserva();
        folio.registrarPago(new Pago(Dinero.pesos(488_000), MedioPago.EFECTIVO, AHORA, null));
        folio.cerrar(AHORA);

        // Act + Assert
        assertThatThrownBy(() -> folio.registrarPago(new Pago(Dinero.pesos(1_000), MedioPago.EFECTIVO, AHORA, null)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("FOLIO_CERRADO");
    }

    @Test
    @DisplayName("7.5 · Al cancelar se anula el alojamiento y la retención queda como penalidad")
    void liquidarCancelacionAnulaAlojamientoYRegistraPenalidad() {
        // Arrange (ejemplo del Anexo B: $704.000, cancelación con 10 días, anticipo pagado)
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), new CodigoReserva("RES-2026-00042"),
                Dinero.pesos(704_000), LocalDateTime.of(2026, 10, 1, 10, 0));
        folio.registrarPago(new Pago(Dinero.pesos(211_200), MedioPago.TRANSFERENCIA, AHORA, "TRF-002"));

        // Act
        folio.liquidarCancelacion(Dinero.pesos(211_200), LocalDateTime.of(2026, 12, 3, 9, 0));

        // Assert
        assertThat(folio.cargos()).extracting(Cargo::tipo)
                .containsExactly(TipoCargo.ALOJAMIENTO, TipoCargo.ALOJAMIENTO, TipoCargo.PENALIDAD);
        assertThat(folio.saldo().esCero()).isTrue();
    }
}

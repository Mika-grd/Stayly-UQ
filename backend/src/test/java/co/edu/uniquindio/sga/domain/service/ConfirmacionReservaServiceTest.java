package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmacionReservaServiceTest {

    private final ConfirmacionReservaService servicio =
            new ConfirmacionReservaService(ConfiguracionAlojamientoFake.ficha());
    private Reserva reserva;
    private Folio folio;

    @BeforeEach
    void prepararReservaDelAnexoB() {
        reserva = FichaStaylyUq.reservaPendiente(FichaStaylyUq.suq201(), diciembre(13, 17), FichaStaylyUq.valorAnexoB());
        reserva.indicarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(16, 0)), "huesped", HOY);
        folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva.codigo(), reserva.valor().total(), HOY);
    }

    @Test
    @DisplayName("L-11 · No confirma si el folio no tiene pagado el anticipo del 30 %")
    void noConfirmarSinAnticipo() {
        // Arrange
        folio.registrarPago(new Pago(Dinero.pesos(200_000), MedioPago.TRANSFERENCIA, HOY.plusHours(2), "TRF-010"));

        // Act + Assert
        assertThatThrownBy(() -> servicio.confirmar(reserva, folio, "recepcion", HOY.plusHours(3)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("ANTICIPO_INSUFICIENTE");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("L-11 · Confirma con el anticipo completo ($211.200 de $704.000)")
    void confirmarConAnticipoCompleto() {
        // Arrange
        folio.registrarPago(new Pago(Dinero.pesos(211_200), MedioPago.TRANSFERENCIA, HOY.plusHours(2), "TRF-011"));

        // Act
        servicio.confirmar(reserva, folio, "recepcion", HOY.plusHours(3));

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
    }
}

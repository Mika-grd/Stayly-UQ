package co.edu.uniquindio.sga.domain.model.reserva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoReservaTest {

    @Test
    @DisplayName("PU-04 · Rechaza la transición desde un estado terminal (RN-08)")
    void rechazarTransicionDesdeEstadoTerminal() {
        // Arrange
        EstadoReserva cancelada = EstadoReserva.CANCELADA;
        EstadoReserva pendiente = EstadoReserva.PENDIENTE;

        // Act
        boolean canceladaAEnCurso = cancelada.puedeTransicionarA(EstadoReserva.EN_CURSO);
        boolean pendienteAConfirmada = pendiente.puedeTransicionarA(EstadoReserva.CONFIRMADA);

        // Assert
        assertThat(canceladaAEnCurso).isFalse();
        assertThat(cancelada.esTerminal()).isTrue();
        assertThat(pendienteAConfirmada).isTrue();
    }
}

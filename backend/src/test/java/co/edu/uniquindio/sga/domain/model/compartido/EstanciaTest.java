package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstanciaTest {

    @Test
    @DisplayName("PU-01 · Rechaza una estancia de cero noches (RN-03)")
    void rechazarEstanciaDeCeroNoches() {
        // Arrange
        LocalDate mismoDia = LocalDate.of(2026, 12, 10);

        // Act + Assert
        assertThatThrownBy(() -> new Estancia(mismoDia, mismoDia))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("La fecha de salida debe ser posterior a la fecha de entrada.")
                .extracting("codigo").isEqualTo("ESTANCIA_SIN_NOCHES");
    }

    @Test
    @DisplayName("PU-02 · Detecta el solapamiento por una sola noche (RN-01, 3.1)")
    void detectarSolapamientoPorUnaNoche() {
        // Arrange
        Estancia a = new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
        Estancia b = new Estancia(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 14));
        Estancia c = new Estancia(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 14));

        // Act
        boolean aConB = a.seSolapaCon(b);
        boolean aConC = a.seSolapaCon(c);

        // Assert
        assertThat(aConB).as("A y B comparten la noche del 11").isTrue();
        assertThat(aConC).as("el que sale el 12 libera esa noche").isFalse();
    }
}

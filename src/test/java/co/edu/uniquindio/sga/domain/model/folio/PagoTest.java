package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagoTest {

    @Test
    @DisplayName("RN-15 · Rechaza un pago sin medio de pago (F-4)")
    void rechazarPagoSinMedio() {
        // Arrange
        Dinero valor = Dinero.pesos(211_200);
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 12, 0);

        // Act + Assert
        assertThatThrownBy(() -> new Pago(valor, null, fecha, "TRF-001"))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("PARAMETRO_INVALIDO");
    }
}

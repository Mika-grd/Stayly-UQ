package co.edu.uniquindio.sga.domain.model.bloqueo;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloqueoTest {

    private static final RangoFechas DICIEMBRE = new RangoFechas(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5));
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 1, 10, 0);

    // Reconstitución desde la persistencia

    @Test
    @DisplayName("Persistencia · Un bloqueo reconstituido como levantado ya no afecta sus noches (RN-07)")
    void reconstituirBloqueoLevantado() {
        // Act
        Bloqueo bloqueo = Bloqueo.reconstituir(new IdBloqueo(UUID.randomUUID()), new IdentificacionApartamento("SUQ-201"),
                DICIEMBRE, "Mantenimiento de tubería", "admin@stayly-uq.co", REGISTRO, true);

        // Assert
        assertThat(bloqueo.estaLevantado()).isTrue();
        assertThat(bloqueo.afecta(new Estancia(LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 4)))).isFalse();
    }

    @Test
    @DisplayName("Persistencia · No reconstituye un bloqueo sin motivo (B-1)")
    void noReconstituirBloqueoSinMotivo() {
        assertThatThrownBy(() -> Bloqueo.reconstituir(new IdBloqueo(UUID.randomUUID()),
                new IdentificacionApartamento("SUQ-201"), DICIEMBRE, " ", "admin@stayly-uq.co", REGISTRO, false))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("PARAMETRO_INVALIDO");
    }
}

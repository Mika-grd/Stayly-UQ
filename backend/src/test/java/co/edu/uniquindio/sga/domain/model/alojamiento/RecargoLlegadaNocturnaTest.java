package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RecargoLlegadaNocturnaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 10, 0);
    private final RecargoLlegadaNocturna recargo = new RecargoLlegadaNocturna(LocalTime.of(21, 0), Dinero.pesos(30_000));

    @Test
    @DisplayName("RP-03 · Aplica el recargo desde la hora de inicio (21:00)")
    void aplicarRecargoDesdeHoraInicio() {
        // Arrange
        HoraEstimadaLlegada alasNueve = new HoraEstimadaLlegada(LocalTime.of(21, 0));

        // Act
        Optional<Cargo> cargo = recargo.cargoPorCambioDeHora(null, alasNueve, AHORA);

        // Assert
        assertThat(recargo.aplicaA(alasNueve)).isTrue();
        assertThat(cargo).hasValueSatisfying(c -> {
            assertThat(c.tipo()).isEqualTo(TipoCargo.SERVICIO_ADICIONAL);
            assertThat(c.valor()).isEqualTo(Dinero.pesos(30_000));
        });
    }

    @Test
    @DisplayName("RP-03 · No aplica el recargo antes de la hora de inicio")
    void noAplicarAntesDeHoraInicio() {
        // Arrange
        HoraEstimadaLlegada antesDeLasNueve = new HoraEstimadaLlegada(LocalTime.of(20, 59));

        // Act
        Optional<Cargo> cargo = recargo.cargoPorCambioDeHora(null, antesDeLasNueve, AHORA);

        // Assert
        assertThat(recargo.aplicaA(antesDeLasNueve)).isFalse();
        assertThat(cargo).isEmpty();
    }

    @Test
    @DisplayName("RP-03 · Revierte el recargo con un movimiento inverso si deja de ser nocturna")
    void revertirSiDejaDeSerNocturna() {
        // Arrange
        HoraEstimadaLlegada nocturna = new HoraEstimadaLlegada(LocalTime.of(22, 30));
        HoraEstimadaLlegada deTarde = new HoraEstimadaLlegada(LocalTime.of(17, 0));

        // Act
        Optional<Cargo> reverso = recargo.cargoPorCambioDeHora(nocturna, deTarde, AHORA);
        Optional<Cargo> sigueNocturna = recargo.cargoPorCambioDeHora(nocturna, new HoraEstimadaLlegada(LocalTime.of(23, 0)), AHORA);

        // Assert
        assertThat(reverso).hasValueSatisfying(c -> assertThat(c.valor()).isEqualTo(Dinero.pesos(-30_000)));
        assertThat(sigueNocturna).as("nunca queda más de un recargo vigente").isEmpty();
    }
}

package co.edu.uniquindio.sga.domain.model.temporada;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporadaTest {

    // Reconstitución desde la persistencia

    @Test
    @DisplayName("Persistencia · Reconstituye la temporada base con la estancia mínima que le cambió el administrador")
    void reconstituirBaseConEstanciaMinima() {
        // Act
        Temporada base = Temporada.reconstituir(new IdTemporada(UUID.randomUUID()), "Base", List.of(), true,
                new EstanciaMinima(2));

        // Assert: crearBase la dejaría siempre en 1 noche
        assertThat(base.esBase()).isTrue();
        assertThat(base.estanciaMinima()).isEqualTo(new EstanciaMinima(2));
    }

    @Test
    @DisplayName("Persistencia · No reconstituye una temporada con periodos que se solapan (T-1)")
    void noReconstituirConPeriodosSolapados() {
        // Arrange
        List<RangoFechas> periodos = List.of(
                new RangoFechas(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15)),
                new RangoFechas(LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 20)));

        // Act + Assert
        assertThatThrownBy(() -> Temporada.reconstituir(new IdTemporada(UUID.randomUUID()), "Alta", periodos, false,
                new EstanciaMinima(2)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TEMPORADAS_SOLAPADAS");
    }
}

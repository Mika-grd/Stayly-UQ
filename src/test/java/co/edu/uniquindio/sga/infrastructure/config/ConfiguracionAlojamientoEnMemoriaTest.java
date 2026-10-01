package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConfiguracionAlojamientoEnMemoriaTest {

    @Autowired
    private ConfiguracionAlojamiento configuracion;

    @Test
    @DisplayName("Sección 6 · Los valores de la Ficha se leen de application.yml, no del código")
    void leerValoresDeLaFichaDesdeApplicationYml() {
        // Arrange: el contexto de Spring carga application.yml

        // Act
        var horario = configuracion.horario();

        // Assert
        assertThat(configuracion.umbralEdad().anios()).isEqualTo(12);
        assertThat(horario.horaEntrada()).isEqualTo(LocalTime.of(15, 0));
        assertThat(horario.horaSalida()).isEqualTo(LocalTime.of(12, 0));
        assertThat(configuracion.tiempoPreparacion().cabeEn(horario)).isTrue();
        assertThat(configuracion.plazoConfirmacion().horas()).isEqualTo(24);
        assertThat(configuracion.horaLimiteNoShow().hora()).isEqualTo(LocalTime.of(23, 0));
        assertThat(configuracion.anticipo().porcentaje()).isEqualTo(30);
        assertThat(configuracion.cupoMascotas().maximo()).isEqualTo(2);
        assertThat(configuracion.cargoPorMascota().valorPorNoche()).isEqualTo(Dinero.pesos(25_000));
        assertThat(configuracion.recargoLlegadaNocturna().horaInicio()).isEqualTo(LocalTime.of(21, 0));
    }
}

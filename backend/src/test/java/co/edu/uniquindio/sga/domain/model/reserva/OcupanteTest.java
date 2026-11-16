package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OcupanteTest {

    @Test
    @DisplayName("PU-07 · No es facturable si cumple 12 años durante la estancia (RN-06)")
    void noFacturableSiCumple12DuranteLaEstancia() {
        // Arrange
        Ocupante ocupante = Ocupante.acompanante(new IdOcupante(UUID.randomUUID()), "Sofía Ríos",
                LocalDate.of(2014, 12, 20), null);
        LocalDate entrada = LocalDate.of(2026, 12, 18);
        UmbralEdadFacturable umbral = new UmbralEdadFacturable(12);

        // Act
        boolean facturable = ocupante.esFacturableEn(entrada, umbral);

        // Assert
        assertThat(facturable).as("tiene 11 años el día de entrada (estancia 18–22/12/2026)").isFalse();
    }
}

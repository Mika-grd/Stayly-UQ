package co.edu.uniquindio.sga.domain.model.apartamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentificacionApartamentoTest {

    @Test
    @DisplayName("PU-03 · Normaliza la identificación a mayúsculas (L-04)")
    void normalizarAMayusculas() {
        // Arrange
        IdentificacionApartamento minusculas = new IdentificacionApartamento("suq-201");
        IdentificacionApartamento mayusculas = new IdentificacionApartamento("SUQ-201");

        // Act
        boolean sonIguales = minusculas.equals(mayusculas);

        // Assert
        assertThat(sonIguales).isTrue();
        assertThat(minusculas.valor()).isEqualTo("SUQ-201");
    }
}

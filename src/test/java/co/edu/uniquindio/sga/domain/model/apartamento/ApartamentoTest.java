package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApartamentoTest {

    @Test
    @DisplayName("7.3 · Rechaza un apartamento sin imágenes (A-4)")
    void rechazarApartamentoSinImagenes() {
        // Arrange
        List<ImagenApartamento> sinImagenes = List.of();

        // Act + Assert
        assertThatThrownBy(() -> Apartamento.crear(new IdentificacionApartamento("SUQ-101"), "Apartamento 101",
                "Un dormitorio con balcón", 1, new Capacidad(2), false, null, Set.of(), sinImagenes))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("IMAGENES_INVALIDAS");
    }

    @Test
    @DisplayName("7.3 · Rechaza un apartamento con once imágenes (A-4)")
    void rechazarOnceImagenes() {
        // Arrange
        List<ImagenApartamento> onceImagenes = FichaStaylyUq.imagenes(11);

        // Act + Assert
        assertThatThrownBy(() -> Apartamento.crear(new IdentificacionApartamento("SUQ-101"), "Apartamento 101",
                "Un dormitorio con balcón", 1, new Capacidad(2), false, null, Set.of(), onceImagenes))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("IMAGENES_INVALIDAS");
    }
}

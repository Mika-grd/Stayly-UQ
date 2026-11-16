package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginaTest {

    private final List<Integer> veintitres = IntStream.rangeClosed(1, 23).boxed().toList();

    @Test
    @DisplayName("12.4 · Los listados se paginan de a 10 y la última página trae el resto")
    void paginarDeADiez() {
        // Act
        Pagina<Integer> primera = Pagina.de(veintitres, PeticionPagina.primera());
        Pagina<Integer> tercera = Pagina.de(veintitres, new PeticionPagina(2));

        // Assert
        assertThat(primera.contenido()).hasSize(10).startsWith(1).endsWith(10);
        assertThat(tercera.contenido()).containsExactly(21, 22, 23);
        assertThat(primera.totalElementos()).isEqualTo(23);
        assertThat(primera.totalPaginas()).isEqualTo(3);
    }

    @Test
    @DisplayName("12.4 · Una página más allá del final viene vacía; una negativa se rechaza")
    void paginaFueraDeRango() {
        assertThat(Pagina.de(veintitres, new PeticionPagina(5)).contenido()).isEmpty();
        assertThatThrownBy(() -> new PeticionPagina(-1))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("PARAMETRO_INVALIDO");
    }
}

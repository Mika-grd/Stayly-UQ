package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificadorEstanciaMinimaServiceTest {

    private final TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
    private final VerificadorEstanciaMinimaService servicio = new VerificadorEstanciaMinimaService(temporadas);

    @BeforeEach
    void cargarTemporadasDeLaFicha() {
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
    }

    @Test
    @DisplayName("RP-01 · Rechaza una noche con entrada en temporada alta")
    void rechazarUnaNocheEnTemporadaAlta() {
        // Arrange (noche del 20/12/2026: temporada alta, exige 2 noches)

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarEstanciaMinima(diciembre(20, 21)))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("La temporada exige una estancia mínima de 2 noches.")
                .extracting("codigo").isEqualTo("ESTANCIA_MINIMA_NO_CUMPLIDA");
    }

    @Test
    @DisplayName("RP-01 · Permite dos noches con entrada en temporada alta")
    void permitirDosNochesEnTemporadaAlta() {
        // Act + Assert
        assertThatCode(() -> servicio.verificarEstanciaMinima(diciembre(20, 22))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RP-01 · Permite una noche en temporada base aunque la salida caiga en alta")
    void permitirUnaNocheEnTemporadaBase() {
        // Act + Assert
        assertThatCode(() -> servicio.verificarEstanciaMinima(diciembre(14, 15))).doesNotThrowAnyException();
    }
}

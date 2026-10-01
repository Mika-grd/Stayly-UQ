package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoOperativoTest {

    @Test
    @DisplayName("7.6 · Sigue el ciclo pendiente → en preparación → preparado → ocupado → pendiente")
    void seguirCicloDePreparacion() {
        // Arrange
        Apartamento apartamento = Apartamento.crear(new IdentificacionApartamento("SUQ-202"), "Apartamento 202",
                "Dos dormitorios, admite mascotas", 2, new Capacidad(4), true, null, Set.of(),
                FichaStaylyUq.imagenes(2));

        // Act
        apartamento.iniciarPreparacion();
        apartamento.marcarPreparado();
        apartamento.marcarOcupado();
        apartamento.liberar();

        // Assert
        assertThat(apartamento.estadoOperativo()).isEqualTo(EstadoOperativo.PENDIENTE_PREPARACION);
        assertThat(EstadoOperativo.PREPARADO.permiteRegistro()).isTrue();
        assertThat(EstadoOperativo.PREPARADO.puedeTransicionarA(EstadoOperativo.EN_PREPARACION)).isFalse();
    }

    @Test
    @DisplayName("7.6 · No declara fuera de servicio un apartamento ocupado (A-3)")
    void noDeclararFueraDeServicioOcupado() {
        // Arrange
        Apartamento apartamento = FichaStaylyUq.suq201();
        apartamento.marcarOcupado();

        // Act + Assert
        assertThatThrownBy(apartamento::declararFueraDeServicio)
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_OCUPADO");
        assertThat(EstadoOperativo.OCUPADO.puedeTransicionarA(EstadoOperativo.FUERA_DE_SERVICIO)).isFalse();
    }

    @Test
    @DisplayName("7.6 · Al volver a servicio pasa a PENDIENTE_PREPARACION, nunca directo a PREPARADO")
    void volverAServicioPasaAPendientePreparacion() {
        // Arrange
        Apartamento apartamento = FichaStaylyUq.suq201();
        apartamento.declararFueraDeServicio();

        // Act
        apartamento.volverAServicio();

        // Assert
        assertThat(apartamento.estadoOperativo()).isEqualTo(EstadoOperativo.PENDIENTE_PREPARACION);
        assertThat(EstadoOperativo.FUERA_DE_SERVICIO.puedeTransicionarA(EstadoOperativo.PREPARADO)).isFalse();
    }
}

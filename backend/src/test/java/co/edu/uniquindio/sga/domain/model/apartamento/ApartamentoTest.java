package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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

    // Reconstitución desde la persistencia

    @Test
    @DisplayName("Persistencia · Reconstituye un apartamento activo y OCUPADO que no puede salir de servicio (A-3)")
    void reconstituirApartamentoOcupado() {
        // Arrange
        Apartamento original = FichaStaylyUq.suq201();

        // Act
        Apartamento apartamento = Apartamento.reconstituir(original.identificacion(), original.nombre(),
                original.descripcion(), original.dormitorios(), original.capacidad(), original.admiteMascotas(),
                original.advertenciaAcceso().orElse(null), original.caracteristicas(), original.imagenes(),
                EstadoOperativo.OCUPADO, true);

        // Assert
        assertThat(apartamento.estaActivo()).isTrue();
        assertThat(apartamento.estadoOperativo()).isEqualTo(EstadoOperativo.OCUPADO);
        assertThatThrownBy(apartamento::declararFueraDeServicio)
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_OCUPADO");
    }

    @Test
    @DisplayName("Persistencia · No reconstituye un apartamento sin imágenes (A-4)")
    void noReconstituirApartamentoSinImagenes() {
        assertThatThrownBy(() -> Apartamento.reconstituir(new IdentificacionApartamento("SUQ-101"),
                "Apartamento 101", "Un dormitorio con balcón", 1, new Capacidad(2), false, null, Set.of(), List.of(),
                EstadoOperativo.PREPARADO, true))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("IMAGENES_INVALIDAS");
    }

    // Actualización de los datos descriptivos (7.3)

    @Test
    @DisplayName("7.3 · Actualizar los datos conserva la identificación, el estado operativo y el indicador de activo")
    void actualizarDatosConservaIdentidadYEstado() {
        // Arrange
        Apartamento suq201 = FichaStaylyUq.suq201();

        // Act
        suq201.actualizarDatos("Apartamento 201 renovado", "Con escritorio nuevo", 2, new Capacidad(3), true,
                "Escaleras angostas", Set.of(new Caracteristica("Escritorio")), FichaStaylyUq.imagenes(3));

        // Assert
        assertThat(suq201.identificacion()).isEqualTo(new IdentificacionApartamento("SUQ-201"));
        assertThat(suq201.nombre()).isEqualTo("Apartamento 201 renovado");
        assertThat(suq201.capacidad()).isEqualTo(new Capacidad(3));
        assertThat(suq201.admiteMascotas()).isTrue();
        assertThat(suq201.advertenciaAcceso()).contains("Escaleras angostas");
        assertThat(suq201.imagenes()).hasSize(3);
        assertThat(suq201.estadoOperativo()).isEqualTo(EstadoOperativo.PREPARADO);
        assertThat(suq201.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("7.3 · Actualizar con dos imágenes principales se rechaza y no cambia nada (A-4)")
    void noActualizarConDosImagenesPrincipales() {
        // Arrange
        Apartamento suq201 = FichaStaylyUq.suq201();
        List<ImagenApartamento> dosPrincipales = List.of(new ImagenApartamento("https://stayly-uq.co/a.jpg", true),
                new ImagenApartamento("https://stayly-uq.co/b.jpg", true));

        // Act + Assert
        assertThatThrownBy(() -> suq201.actualizarDatos("Otro nombre", "x", 2, new Capacidad(4), false, null,
                Set.of(), dosPrincipales))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("IMAGENES_INVALIDAS");
        assertThat(suq201.nombre()).isEqualTo("Apartamento 201");
    }
}

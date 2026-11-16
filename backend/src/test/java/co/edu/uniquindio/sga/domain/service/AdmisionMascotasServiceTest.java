package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmisionMascotasServiceTest {

    private final AdmisionMascotasService servicio = new AdmisionMascotasService(ConfiguracionAlojamientoFake.ficha());

    @Test
    @DisplayName("RP-02 · Rechaza mascotas en un apartamento que no las admite (SUQ-201)")
    void rechazarMascotaEnApartamentoQueNoAdmite() {
        // Arrange
        MascotasAutorizadas unaMascota = new MascotasAutorizadas(1);

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarMascotas(FichaStaylyUq.suq201(), unaMascota))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("MASCOTAS_NO_ADMITIDAS");
    }

    @Test
    @DisplayName("RP-02 · Rechaza más mascotas que el cupo (2 por reserva)")
    void rechazarExcesoDeCupo() {
        // Arrange
        MascotasAutorizadas tresMascotas = new MascotasAutorizadas(3);

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarMascotas(FichaStaylyUq.suq102(), tresMascotas))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("Se admiten como máximo 2 mascotas por reserva.")
                .extracting("codigo").isEqualTo("CUPO_MASCOTAS_EXCEDIDO");
    }

    @Test
    @DisplayName("RP-02 · Calcula el cargo por mascota por noche: 2 × 3 noches × $25.000")
    void calcularCargoPorMascotaPorNoche() {
        // Arrange
        MascotasAutorizadas dosMascotas = new MascotasAutorizadas(2);

        // Act
        servicio.verificarMascotas(FichaStaylyUq.suq301(), dosMascotas);
        Dinero cargo = servicio.cargoPorMascotas(dosMascotas, diciembre(5, 8));

        // Assert
        assertThat(cargo).isEqualTo(Dinero.pesos(150_000));
    }
}

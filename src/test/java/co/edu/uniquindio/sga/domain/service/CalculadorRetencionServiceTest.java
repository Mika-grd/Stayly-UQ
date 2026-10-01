package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.PoliticaCancelacionRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadorRetencionServiceTest {

    private final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria();
    private final CalculadorRetencionService servicio = new CalculadorRetencionService(politicas);
    private PoliticaCancelacion politicaV1;
    private Reserva reserva;

    @BeforeEach
    void prepararReservaDelAnexoB() {
        politicaV1 = FichaStaylyUq.politicaV1();
        politicas.guardar(politicaV1);
        reserva = FichaStaylyUq.reservaPendiente(FichaStaylyUq.suq201(), diciembre(13, 17), FichaStaylyUq.valorAnexoB());
    }

    @Test
    @DisplayName("RN-13 · Usa la versión de política congelada en la reserva, no la vigente")
    void usarVersionCongeladaNoLaVigente() {
        // Arrange
        politicas.guardar(politicaV1.nuevaVersion(List.of(new TramoPolitica(30, 0), new TramoPolitica(0, 100)), 100,
                LocalDateTime.of(2026, 11, 1, 0, 0)));
        LocalDateTime diezDiasAntes = LocalDateTime.of(2026, 12, 3, 9, 0);

        // Act
        Dinero retencion = servicio.calcularRetencion(reserva, diezDiasAntes);

        // Assert
        assertThat(politicas.buscarVigente().version().numero()).isEqualTo(2);
        assertThat(retencion).as("30 %% de $704.000 según la versión 1").isEqualTo(Dinero.pesos(211_200));
        assertThat(servicio.calcularPenalidadNoShow(reserva)).isEqualTo(Dinero.pesos(211_200));
    }

    @Test
    @DisplayName("7.5 · Calcula la retención según el tramo de antelación")
    void calcularRetencionPorTramo() {
        // Arrange
        LocalDateTime veinteDiasAntes = LocalDateTime.of(2026, 11, 23, 9, 0);
        LocalDateTime diezDiasAntes = LocalDateTime.of(2026, 12, 3, 9, 0);
        LocalDateTime tresDiasAntes = LocalDateTime.of(2026, 12, 10, 9, 0);

        // Act
        Dinero conVeinte = servicio.calcularRetencion(reserva, veinteDiasAntes);
        Dinero conDiez = servicio.calcularRetencion(reserva, diezDiasAntes);
        Dinero conTres = servicio.calcularRetencion(reserva, tresDiasAntes);

        // Assert
        assertThat(conVeinte).isEqualTo(Dinero.cero());
        assertThat(conDiez).isEqualTo(Dinero.pesos(211_200));
        assertThat(conTres).isEqualTo(Dinero.pesos(422_400));
    }
}

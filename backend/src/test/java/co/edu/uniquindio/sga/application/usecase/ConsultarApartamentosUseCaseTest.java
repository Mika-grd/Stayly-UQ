package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentosUseCase.ApartamentoInventario;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultarApartamentosUseCaseTest {

    @Test
    @DisplayName("7.3 · El inventario del administrador incluye los inactivos y dice si tienen tarifas completas")
    void inventarioConInactivos() {
        // Arrange: SUQ-201 activo con tarifas; SUQ-102 retirado (inactivo) y sin tarifas
        var apartamentos = new ApartamentoRepositoryEnMemoria();
        var temporadas = new TemporadaRepositoryEnMemoria();
        var tarifarios = new TarifarioRepositoryEnMemoria();
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        tarifarios.guardar(FichaStaylyUq.tarifarioSuq201());
        apartamentos.guardar(FichaStaylyUq.suq201());
        var suq102 = FichaStaylyUq.suq102();
        suq102.retirar();
        apartamentos.guardar(suq102);

        // Act
        List<ApartamentoInventario> filas = new ConsultarApartamentosUseCase(apartamentos,
                new VerificadorTarifasCompletasService(temporadas, tarifarios))
                .ejecutar(PeticionPagina.primera()).contenido();

        // Assert
        assertThat(filas).extracting(fila -> fila.apartamento().identificacion().valor())
                .containsExactly("SUQ-102", "SUQ-201");
        assertThat(filas.get(0).apartamento().estaActivo()).isFalse();
        assertThat(filas.get(0).tarifasCompletas()).isFalse();
        assertThat(filas.get(1).apartamento().estaActivo()).isTrue();
        assertThat(filas.get(1).tarifasCompletas()).isTrue();
    }
}

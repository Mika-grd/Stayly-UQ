package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.reserva.CargoNoche;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;

class CotizadorEstanciaServiceTest {

    @Test
    @DisplayName("PU-11 · Liquida una estancia que cruza dos temporadas (RN-05, RN-06)")
    void liquidarEstanciaQueCruzaDosTemporadas() {
        // Arrange
        TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
        tarifarios.guardar(FichaStaylyUq.tarifarioSuq201());
        CotizadorEstanciaService cotizador =
                new CotizadorEstanciaService(temporadas, tarifarios, ConfiguracionAlojamientoFake.ficha());
        ComposicionGrupo dosAdultosYUnNinoDe6 = new ComposicionGrupo(List.of(
                LocalDate.of(1990, 5, 10), LocalDate.of(1988, 2, 1), LocalDate.of(2020, 3, 15)));

        // Act
        ValorCongelado valor = cotizador.cotizar(new IdentificacionApartamento("SUQ-201"), diciembre(14, 16),
                dosAdultosYUnNinoDe6);

        // Assert
        assertThat(valor.noches()).hasSize(2);
        assertThat(valor.noches()).extracting(CargoNoche::temporada).containsExactly("Base", "Alta");
        assertThat(valor.noches()).extracting(CargoNoche::tarifa)
                .containsExactly(Dinero.pesos(75_000), Dinero.pesos(101_000));
        assertThat(valor.noches()).extracting(CargoNoche::ocupantesFacturables).containsOnly(2);
        assertThat(valor.total()).isEqualTo(Dinero.pesos(352_000));
    }
}

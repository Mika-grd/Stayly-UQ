package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.application.usecase.GestionarTarifasUseCase.TarifaDefinida;
import co.edu.uniquindio.sga.application.usecase.GestionarTarifasUseCase.TarifarioConsultado;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasUseCase.DatosTemporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.PoliticaCancelacionRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.ID_ALTA;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.ID_BASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 7.4 de punta a punta: temporadas, tarifas y política, con repositorios en memoria y un reloj fijo. */
class TemporadaTarifaPoliticaUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
    private final TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
    private final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria();
    private final GestionarTemporadasUseCase gestionarTemporadas =
            new GestionarTemporadasUseCase(temporadas, new CalendarioTemporadasService(temporadas));
    private final GestionarTarifasUseCase gestionarTarifas = new GestionarTarifasUseCase(apartamentos, temporadas,
            tarifarios, new VerificadorTarifasCompletasService(temporadas, tarifarios), reloj);

    @BeforeEach
    void cargarFicha() {
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        apartamentos.guardar(FichaStaylyUq.suq201());
        politicas.guardar(FichaStaylyUq.politicaV1());
    }

    @Test
    @DisplayName("7.4 · Una temporada nueva sin solaparse se crea con su estancia mínima")
    void crearTemporada() {
        // Act
        Temporada media = gestionarTemporadas.crear(new DatosTemporada("Media", List.of(
                new RangoFechas(LocalDate.of(2027, 6, 15), LocalDate.of(2027, 7, 16))), false, null));

        // Assert
        assertThat(media.estanciaMinima()).as("por defecto 1 noche").isEqualTo(new EstanciaMinima(1));
        assertThat(gestionarTemporadas.listar()).hasSize(3);
    }

    @Test
    @DisplayName("7.4 · No se crea una temporada que se solapa con la alta")
    void noCrearTemporadaSolapada() {
        assertThatThrownBy(() -> gestionarTemporadas.crear(new DatosTemporada("Navidad", List.of(
                new RangoFechas(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 27))), false, null)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TEMPORADAS_SOLAPADAS");
    }

    @Test
    @DisplayName("F-05 · No se crea una segunda temporada base, ni una base con periodos")
    void unaSolaBaseSinPeriodos() {
        assertThatThrownBy(() -> gestionarTemporadas.crear(new DatosTemporada("Otra base", List.of(), true, null)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TEMPORADA_BASE_DUPLICADA");
        assertThatThrownBy(() -> gestionarTemporadas.actualizar(ID_BASE, new DatosTemporada("Base", List.of(
                new RangoFechas(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 5))), true, null)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("PARAMETRO_INVALIDO");
    }

    @Test
    @DisplayName("7.4 · Reemplazar una temporada conserva su identificador; una inexistente es 404")
    void actualizarTemporada() {
        // Act
        Temporada alta = gestionarTemporadas.actualizar(ID_ALTA, new DatosTemporada("Alta", List.of(
                new RangoFechas(LocalDate.of(2026, 12, 18), LocalDate.of(2027, 1, 10))), false, new EstanciaMinima(3)));

        // Assert
        assertThat(alta.id()).isEqualTo(ID_ALTA);
        assertThat(temporadas.buscarPorId(ID_ALTA).orElseThrow().estanciaMinima()).isEqualTo(new EstanciaMinima(3));
        assertThatThrownBy(() -> gestionarTemporadas.actualizar(new IdTemporada(UUID.randomUUID()),
                new DatosTemporada("X", List.of(), false, null)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .extracting("codigo").isEqualTo("TEMPORADA_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("RP-01 · La estancia mínima se cambia por configuración")
    void configurarEstanciaMinima() {
        // Act
        Temporada base = new ConfigurarEstanciaMinimaUseCase(temporadas).ejecutar(ID_BASE, new EstanciaMinima(2));

        // Assert
        assertThat(base.estanciaMinima()).isEqualTo(new EstanciaMinima(2));
    }

    @Test
    @DisplayName("TA-2 · Definir tarifas conserva el histórico e informa si el tarifario quedó completo")
    void definirTarifas() {
        // Arrange
        IdentificacionApartamento suq201 = FichaStaylyUq.suq201().identificacion();

        // Act
        TarifarioConsultado soloBase = gestionarTarifas.definir(suq201,
                List.of(new TarifaDefinida(ID_BASE, Dinero.pesos(75_000))));
        TarifarioConsultado completo = gestionarTarifas.definir(suq201, List.of(
                new TarifaDefinida(ID_BASE, Dinero.pesos(80_000)), new TarifaDefinida(ID_ALTA, Dinero.pesos(101_000))));

        // Assert
        assertThat(soloBase.completo()).isFalse();
        assertThat(completo.completo()).isTrue();
        assertThat(completo.tarifario().historico()).hasSize(3);
        assertThat(gestionarTarifas.consultar(suq201).tarifario().tarifaVigente(ID_BASE).orElseThrow().valor())
                .isEqualTo(Dinero.pesos(80_000));
    }

    @Test
    @DisplayName("TA-1 · Una tarifa en cero o para una temporada inexistente se rechaza")
    void rechazarTarifasInvalidas() {
        IdentificacionApartamento suq201 = FichaStaylyUq.suq201().identificacion();

        assertThatThrownBy(() -> gestionarTarifas.definir(suq201, List.of(new TarifaDefinida(ID_BASE, Dinero.cero()))))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("VALOR_INVALIDO");
        assertThatThrownBy(() -> gestionarTarifas.definir(suq201, List.of(
                new TarifaDefinida(new IdTemporada(UUID.randomUUID()), Dinero.pesos(70_000)))))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .extracting("codigo").isEqualTo("TEMPORADA_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("F-07 · Actualizar la política crea la versión 2 y conserva la 1 (RN-13)")
    void actualizarPolitica() {
        // Act
        PoliticaCancelacion v2 = new ActualizarPoliticaCancelacionUseCase(politicas, reloj).ejecutar(
                List.of(new TramoPolitica(30, 0), new TramoPolitica(0, 50)), 40);

        // Assert
        assertThat(v2.version()).isEqualTo(new VersionPolitica(2));
        assertThat(new ConsultarPoliticaVigenteUseCase(politicas).ejecutar()).isEqualTo(v2);
        assertThat(politicas.buscarPorVersion(new VersionPolitica(1))).isEqualTo(FichaStaylyUq.politicaV1());
    }

    @Test
    @DisplayName("L-10 · Una política con un solo tramo se rechaza")
    void rechazarPoliticaConUnTramo() {
        assertThatThrownBy(() -> new ActualizarPoliticaCancelacionUseCase(politicas, reloj).ejecutar(
                List.of(new TramoPolitica(0, 50)), 40))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TRAMOS_INSUFICIENTES");
    }
}

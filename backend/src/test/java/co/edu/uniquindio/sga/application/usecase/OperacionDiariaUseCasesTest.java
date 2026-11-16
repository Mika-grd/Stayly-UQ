package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.NovedadRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 7.6 con repositorios en memoria: estado operativo, fuera de servicio y novedades. */
class OperacionDiariaUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String SERVICIO = "servicio@stayly.local";
    private static final IdentificacionApartamento SUQ_201 = new IdentificacionApartamento("SUQ-201");

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final NovedadRepositoryEnMemoria novedades = new NovedadRepositoryEnMemoria();
    private Apartamento suq201;

    @BeforeEach
    void cargarApartamento() {
        suq201 = FichaStaylyUq.suq201();
        apartamentos.guardar(suq201);
    }

    @Test
    @DisplayName("CU-09 y CU-10 · Tras la salida el apartamento se prepara en orden hasta quedar PREPARADO")
    void prepararApartamento() {
        // Arrange: el grupo salió y el apartamento quedó pendiente de preparación
        suq201.marcarOcupado();
        suq201.liberar();

        // Act
        CambioEstadoOperativo enPreparacion = new IniciarPreparacionApartamentoUseCase(apartamentos, reloj)
                .ejecutar(SUQ_201, SERVICIO);
        CambioEstadoOperativo preparado = new MarcarApartamentoPreparadoUseCase(apartamentos, reloj)
                .ejecutar(SUQ_201, SERVICIO);

        // Assert
        assertThat(enPreparacion.apartamento().estadoOperativo()).isEqualTo(EstadoOperativo.PREPARADO);
        assertThat(preparado.autor()).isEqualTo(SERVICIO);
        assertThat(preparado.fecha()).isEqualTo(HOY);
        assertThat(apartamentos.buscarPorIdentificacion(SUQ_201).orElseThrow().puedeRecibirGrupo()).isTrue();
    }

    @Test
    @DisplayName("7.6 · No se marca PREPARADO sin haber iniciado la preparación")
    void noSaltarseLaPreparacion() {
        suq201.marcarOcupado();
        suq201.liberar();

        assertThatThrownBy(() -> new MarcarApartamentoPreparadoUseCase(apartamentos, reloj).ejecutar(SUQ_201, SERVICIO))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TRANSICION_ESTADO_OPERATIVO_INVALIDA");
    }

    @Test
    @DisplayName("CU-15 y CU-21 · Fuera de servicio deja su motivo como novedad ALTA y vuelve por PENDIENTE_PREPARACION")
    void fueraDeServicioYVueltaAServicio() {
        // Act
        new DeclararFueraDeServicioUseCase(apartamentos, novedades, reloj)
                .ejecutar(SUQ_201, "Fuga en el baño", "admin@stayly.local");
        CambioEstadoOperativo vuelta = new VolverAServicioApartamentoUseCase(apartamentos, reloj)
                .ejecutar(SUQ_201, "admin@stayly.local");

        // Assert
        List<Novedad> registradas = novedades.listarPorApartamento(SUQ_201);
        assertThat(registradas).singleElement().satisfies(novedad -> {
            assertThat(novedad.gravedad()).isEqualTo(GravedadNovedad.ALTA);
            assertThat(novedad.descripcion()).contains("Fuga en el baño");
        });
        assertThat(vuelta.apartamento().estadoOperativo()).isEqualTo(EstadoOperativo.PENDIENTE_PREPARACION);
    }

    @Test
    @DisplayName("A-3 · Un apartamento ocupado no se declara fuera de servicio y no queda novedad")
    void noFueraDeServicioSiEstaOcupado() {
        suq201.marcarOcupado();

        assertThatThrownBy(() -> new DeclararFueraDeServicioUseCase(apartamentos, novedades, reloj)
                .ejecutar(SUQ_201, "Revisión", "admin@stayly.local"))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_OCUPADO");
        assertThat(novedades.listarPorApartamento(SUQ_201)).isEmpty();
    }

    @Test
    @DisplayName("CU-11 y CU-41 · Las novedades no cambian el estado y se listan de la más reciente, de a 10")
    void registrarYConsultarNovedades() {
        // Arrange: 12 novedades, una por minuto
        IntStream.range(0, 12).forEach(minuto -> new RegistrarNovedadUseCase(apartamentos, novedades,
                Clock.offset(reloj, java.time.Duration.ofMinutes(minuto)))
                .ejecutar(SUQ_201, "Novedad " + minuto, GravedadNovedad.BAJA, SERVICIO));

        // Act
        Pagina<Novedad> primera = new ConsultarNovedadesUseCase(apartamentos, novedades)
                .ejecutar(SUQ_201, PeticionPagina.primera());

        // Assert
        assertThat(primera.contenido()).hasSize(10);
        assertThat(primera.totalElementos()).isEqualTo(12);
        assertThat(primera.contenido().getFirst().descripcion()).isEqualTo("Novedad 11");
        assertThat(suq201.estadoOperativo()).isEqualTo(EstadoOperativo.PREPARADO);
    }
}

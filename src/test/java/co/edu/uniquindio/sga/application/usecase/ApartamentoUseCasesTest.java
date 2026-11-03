package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.service.AltaApartamentoService;
import co.edu.uniquindio.sga.domain.service.RegistroBloqueoService;
import co.edu.uniquindio.sga.domain.service.RetiroApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 7.3 de punta a punta: alojamiento, apartamentos y bloqueos, con repositorios en memoria y un reloj fijo. */
class ApartamentoUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final IdentificacionApartamento SUQ_401 = new IdentificacionApartamento("SUQ-401");

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final ConfiguracionAlojamientoFake configuracion = ConfiguracionAlojamientoFake.ficha();
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
    private final TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
    private final BloqueoRepositoryEnMemoria bloqueos = new BloqueoRepositoryEnMemoria();
    private final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria(reloj);
    private final VerificadorTarifasCompletasService verificadorTarifas =
            new VerificadorTarifasCompletasService(temporadas, tarifarios);
    private Apartamento suq201;

    @BeforeEach
    void cargarFicha() {
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        suq201 = FichaStaylyUq.suq201();
        apartamentos.guardar(suq201);
        tarifarios.guardar(FichaStaylyUq.tarifarioSuq201());
    }

    @Test
    @DisplayName("7.3 · Un apartamento nuevo nace inactivo y en PENDIENTE_PREPARACION")
    void crearApartamento() {
        // Act
        Apartamento creado = crear().ejecutar(SUQ_401, datos(4));

        // Assert
        assertThat(creado.estaActivo()).isFalse();
        assertThat(creado.estadoOperativo()).isEqualTo(EstadoOperativo.PENDIENTE_PREPARACION);
        assertThat(apartamentos.buscarPorIdentificacion(SUQ_401)).isPresent();
    }

    @Test
    @DisplayName("L-04 · No se crea un apartamento con una identificación ya usada, sin importar mayúsculas")
    void noCrearIdentificacionDuplicada() {
        assertThatThrownBy(() -> crear().ejecutar(new IdentificacionApartamento("suq-201"), datos(4)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("IDENTIFICACION_DUPLICADA");
    }

    @Test
    @DisplayName("7.3 · Bajar la capacidad no toca las reservas, pero advierte cuáles quedan por encima")
    void actualizarCapacidadAdvierteReservas() {
        // Arrange: reserva de 2 ocupantes en SUQ-201 (capacidad 4)
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15));
        reservas.guardar(reserva);

        // Act: la capacidad baja a 1
        ActualizarApartamentoUseCase.ApartamentoActualizado actualizado =
                new ActualizarApartamentoUseCase(apartamentos, reservas, reloj)
                        .ejecutar(suq201.identificacion(), datos(1));

        // Assert
        assertThat(actualizado.apartamento().capacidad()).isEqualTo(new Capacidad(1));
        assertThat(actualizado.apartamento().nombre()).isEqualTo("Apartamento renovado");
        assertThat(actualizado.reservasSobreNuevaCapacidad()).containsExactly(reserva.codigo());
        assertThat(reserva.totalOcupantes()).as("la reserva no cambia").isEqualTo(2);
    }

    @Test
    @DisplayName("7.4 · No se activa un apartamento sin tarifa en todas las temporadas; con ellas sí")
    void activarSoloConTarifasCompletas() {
        // Arrange
        crear().ejecutar(SUQ_401, datos(4));
        ActivarApartamentoUseCase activar = new ActivarApartamentoUseCase(apartamentos, verificadorTarifas);

        // Act + Assert
        assertThatThrownBy(() -> activar.ejecutar(SUQ_401))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TARIFAS_INCOMPLETAS");
        tarifarios.guardar(FichaStaylyUq.tarifario("SUQ-401", 80_000, 110_000, 95_000));
        assertThat(activar.ejecutar(SUQ_401).estaActivo()).isTrue();
    }

    @Test
    @DisplayName("7.3 · No se retira un apartamento con reservas activas; sin ellas queda inactivo y fuera de los listados")
    void retirarApartamento() {
        // Arrange
        RetirarApartamentoUseCase retirar = new RetirarApartamentoUseCase(apartamentos,
                new RetiroApartamentoService(reservas), reloj);
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15));
        reservas.guardar(reserva);

        // Act + Assert
        assertThatThrownBy(() -> retirar.ejecutar(suq201.identificacion()))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_CON_RESERVAS_ACTIVAS");
        reserva.cancelar("recepcion", HOY);
        retirar.ejecutar(suq201.identificacion());
        assertThat(apartamentos.listarActivos()).doesNotContain(suq201);
        assertThat(apartamentos.buscarPorIdentificacion(suq201.identificacion())).isPresent();
    }

    @Test
    @DisplayName("CU-16 · No se bloquean noches con reservas activas; fuera de ellas sí (7.3, RN-07)")
    void registrarBloqueo() {
        // Arrange
        reservas.guardar(FichaStaylyUq.reservaPendiente(suq201, diciembre(13, 15)));
        RegistrarBloqueoUseCase bloquear = new RegistrarBloqueoUseCase(apartamentos, bloqueos,
                new RegistroBloqueoService(reservas), reloj);

        // Act + Assert
        assertThatThrownBy(() -> bloquear.ejecutar(suq201.identificacion(),
                new RangoFechas(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 18)), "Pintura", "admin"))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("BLOQUEO_SOBRE_RESERVAS_ACTIVAS");
        Bloqueo bloqueo = bloquear.ejecutar(suq201.identificacion(),
                new RangoFechas(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 18)), "Pintura", "admin");
        assertThat(bloqueos.buscarVigentesQueSolapan(suq201.identificacion(), diciembre(16, 17)))
                .containsExactly(bloqueo);
        assertThat(suq201.estadoOperativo()).as("un bloqueo no cambia el estado operativo (3.3)")
                .isEqualTo(EstadoOperativo.PREPARADO);
    }

    @Test
    @DisplayName("7.1 · El detalle trae la ubicación del alojamiento; un apartamento inexistente es 404")
    void consultarDetalle() {
        // Arrange
        ConsultarDetalleApartamentoUseCase consultar = new ConsultarDetalleApartamentoUseCase(apartamentos,
                configuracion);

        // Act
        ConsultarDetalleApartamentoUseCase.DetalleApartamento detalle = consultar.ejecutar(suq201.identificacion());

        // Assert
        assertThat(detalle.apartamento()).isEqualTo(suq201);
        assertThat(detalle.ubicacion()).isEqualTo(configuracion.alojamiento().ubicacion());
        assertThatThrownBy(() -> consultar.ejecutar(new IdentificacionApartamento("SUQ-999")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_NO_ENCONTRADO");
    }

    @Test
    @DisplayName("7.3 · El alojamiento se consulta con sus parámetros de la sección 6")
    void consultarAlojamiento() {
        assertThat(new ConsultarAlojamientoUseCase(configuracion).ejecutar().umbralEdad())
                .isEqualTo(configuracion.umbralEdad());
    }

    private CrearApartamentoUseCase crear() {
        return new CrearApartamentoUseCase(apartamentos, new AltaApartamentoService(apartamentos));
    }

    private static DatosApartamento datos(int capacidad) {
        return new DatosApartamento("Apartamento renovado", "Dos dormitorios", 2, new Capacidad(capacidad), false,
                null, Set.of(new Caracteristica("Wifi")), List.copyOf(FichaStaylyUq.imagenes(2)));
    }
}

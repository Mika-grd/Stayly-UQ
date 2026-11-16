package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.CanalRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ConflictoCanalRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 7.8 con repositorios en memoria: bitácora de canales y revisión de conflictos. */
class CanalUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String ADMIN = "admin@stayly.local";

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final CanalRepositoryEnMemoria canales = new CanalRepositoryEnMemoria();
    private final ConflictoCanalRepositoryEnMemoria conflictos = new ConflictoCanalRepositoryEnMemoria();
    private Canal booking;
    private Canal airbnb;

    @BeforeEach
    void registrarCanales() {
        booking = Canal.registrar(new IdCanal(UUID.randomUUID()), "Booking", CanalOrigen.EXTERNO);
        booking.registrarEvento(new EventoCanal("RESERVA_RECIBIDA", "BKG-1", HOY.minusDays(3), "CREADA"));
        booking.registrarEvento(new EventoCanal("RESERVA_DUPLICADA", "BKG-1", HOY.minusDays(1), "YA_EXISTIA"));
        airbnb = Canal.registrar(new IdCanal(UUID.randomUUID()), "Airbnb", CanalOrigen.EXTERNO);
        airbnb.registrarEvento(new EventoCanal("DISPONIBILIDAD_CONSULTADA", null, HOY.minusDays(2), "3 disponibles"));
        canales.guardar(booking);
        canales.guardar(airbnb);
    }

    @Test
    @DisplayName("7.8 · La bitácora junta los canales del más reciente al más antiguo y filtra por canal y fechas")
    void bitacora() {
        ConsultarBitacoraCanalesUseCase bitacora = new ConsultarBitacoraCanalesUseCase(canales);

        Pagina<ConsultarBitacoraCanalesUseCase.Entrada> todas = bitacora.ejecutar(null, null, null, PeticionPagina.primera());
        Pagina<ConsultarBitacoraCanalesUseCase.Entrada> deBooking = bitacora.ejecutar("booking", null, null,
                PeticionPagina.primera());
        Pagina<ConsultarBitacoraCanalesUseCase.Entrada> recientes = bitacora.ejecutar(null,
                HOY.toLocalDate().minusDays(2), HOY.toLocalDate(), PeticionPagina.primera());

        assertThat(todas.contenido()).extracting(entrada -> entrada.evento().tipo())
                .containsExactly("RESERVA_DUPLICADA", "DISPONIBILIDAD_CONSULTADA", "RESERVA_RECIBIDA");
        assertThat(deBooking.totalElementos()).isEqualTo(2);
        assertThat(recientes.totalElementos()).isEqualTo(2);
    }

    @Test
    @DisplayName("C-1 · El id de un evento no cambia al crecer la bitácora")
    void idEstable() {
        ConsultarBitacoraCanalesUseCase bitacora = new ConsultarBitacoraCanalesUseCase(canales);
        UUID antes = bitacora.ejecutar("Airbnb", null, null, PeticionPagina.primera()).contenido().getFirst().id();

        airbnb.registrarEvento(new EventoCanal("DISPONIBILIDAD_CONSULTADA", null, HOY, "2 disponibles"));
        UUID despues = bitacora.ejecutar("Airbnb", null, null, PeticionPagina.primera()).contenido().getLast().id();

        assertThat(despues).isEqualTo(antes);
    }

    @Test
    @DisplayName("RN-18 · Los conflictos traen el nombre del canal y se filtran por estado")
    void consultarConflictos() {
        ConflictoCanal pendiente = conflicto("BKG-2");
        ConflictoCanal resuelto = conflicto("BKG-3");
        resuelto.resolver(ADMIN, "Reubicado", HOY);
        conflictos.guardar(pendiente);
        conflictos.guardar(resuelto);

        Pagina<ConflictoConCanal> pendientes = new ConsultarConflictosCanalUseCase(conflictos, canales)
                .ejecutar(EstadoConflicto.PENDIENTE, PeticionPagina.primera());

        assertThat(pendientes.contenido()).singleElement().satisfies(conCanal -> {
            assertThat(conCanal.nombreCanal()).isEqualTo("Booking");
            assertThat(conCanal.conflicto().identificadorExterno().codigo()).isEqualTo("BKG-2");
        });
    }

    @Test
    @DisplayName("CC-1 · Resolver deja autor y resolución; otra vez es CONFLICTO_YA_RESUELTO; sin conflicto es 404")
    void resolverConflicto() {
        ConflictoCanal pendiente = conflicto("BKG-4");
        conflictos.guardar(pendiente);
        ResolverConflictoCanalUseCase resolver = new ResolverConflictoCanalUseCase(conflictos, canales, reloj);

        ConflictoConCanal resuelto = resolver.ejecutar(pendiente.id(), ADMIN, "Se avisó al canal");

        assertThat(resuelto.conflicto().estado()).isEqualTo(EstadoConflicto.RESUELTO);
        assertThat(resuelto.conflicto().autorResolucion()).contains(ADMIN);
        assertThatThrownBy(() -> resolver.ejecutar(pendiente.id(), ADMIN, "Otra vez"))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "CONFLICTO_YA_RESUELTO");
        assertThatThrownBy(() -> resolver.ejecutar(new IdConflictoCanal(UUID.randomUUID()), ADMIN, "Nada"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasFieldOrPropertyWithValue("codigo", "CONFLICTO_NO_ENCONTRADO");
    }

    private ConflictoCanal conflicto(String codigoExterno) {
        return ConflictoCanal.registrar(new IdConflictoCanal(UUID.randomUUID()),
                new IdentificadorExterno(booking.id(), codigoExterno), new IdentificacionApartamento("SUQ-201"),
                FichaStaylyUq.diciembre(1, 3), new CodigoReserva("RES-2026-00001"), HOY);
    }
}

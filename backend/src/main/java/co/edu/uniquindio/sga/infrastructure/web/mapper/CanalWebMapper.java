package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.CanalResponse;
import co.edu.uniquindio.sga.application.dto.response.ConflictoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.EventoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaConflictoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaEventoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaExternaResponse;
import co.edu.uniquindio.sga.application.dto.response.ResultadoReservaExterna;
import co.edu.uniquindio.sga.application.usecase.ConflictoConCanal;
import co.edu.uniquindio.sga.application.usecase.ConsultarBitacoraCanalesUseCase;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.TipoEventoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import org.mapstruct.Mapper;

import static co.edu.uniquindio.sga.infrastructure.web.mapper.DominioWebMapperSoporte.dineroALong;

/** Dominio → DTO de canales, reservas externas y conflictos (7.8), sin lógica de negocio. */
@Mapper(componentModel = "spring")
public interface CanalWebMapper {

    default CanalResponse aCanal(Canal canal, boolean activo, String credencialGenerada) {
        return new CanalResponse(canal.id().valor(), canal.nombre(), canal.tipo(), activo, credencialGenerada);
    }

    default EventoCanalResponse aEvento(ConsultarBitacoraCanalesUseCase.Entrada entrada) {
        var evento = entrada.evento();
        return new EventoCanalResponse(entrada.id(), entrada.canal().nombre(), TipoEventoCanal.valueOf(evento.tipo()),
                evento.identificadorExterno(), evento.fecha(), evento.resultado());
    }

    default PaginaEventoCanalResponse aPaginaEventos(Pagina<ConsultarBitacoraCanalesUseCase.Entrada> pagina) {
        return new PaginaEventoCanalResponse(pagina.contenido().stream().map(this::aEvento).toList(), pagina.numero(),
                pagina.tamano(), pagina.totalElementos(), pagina.totalPaginas());
    }

    default ReservaExternaResponse aReservaExterna(String identificadorExterno, Reserva reserva,
                                                   ResultadoReservaExterna resultado) {
        return new ReservaExternaResponse(identificadorExterno, reserva.codigo().valor(), reserva.estado(), resultado,
                dineroALong(reserva.valor().total()));
    }

    default ConflictoCanalResponse aConflicto(ConflictoConCanal conCanal) {
        ConflictoCanal conflicto = conCanal.conflicto();
        return new ConflictoCanalResponse(conflicto.id().valor(), conCanal.nombreCanal(),
                conflicto.identificadorExterno().codigo(), conflicto.apartamento().valor(),
                conflicto.estancia().entrada(), conflicto.estancia().salida(), conflicto.reservaVigente().valor(),
                conflicto.estado(), conflicto.fechaRegistro(), conflicto.resolucion().orElse(null));
    }

    default PaginaConflictoCanalResponse aPaginaConflictos(Pagina<ConflictoConCanal> pagina) {
        return new PaginaConflictoCanalResponse(pagina.contenido().stream().map(this::aConflicto).toList(),
                pagina.numero(), pagina.tamano(), pagina.totalElementos(), pagina.totalPaginas());
    }
}

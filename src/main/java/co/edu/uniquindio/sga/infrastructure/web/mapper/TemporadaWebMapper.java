package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.TemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasUseCase.DatosTemporada;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import org.mapstruct.Mapper;

import java.util.List;

/** DTO ↔ dominio del recurso Temporadas (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface TemporadaWebMapper {

    default DatosTemporada aDatos(TemporadaRequest request) {
        return new DatosTemporada(request.nombre(), aPeriodos(request), request.esBase(),
                request.estanciaMinima() == null ? null : new EstanciaMinima(request.estanciaMinima()));
    }

    default List<RangoFechas> aPeriodos(TemporadaRequest request) {
        return request.periodos() == null ? List.of()
                : request.periodos().stream().map(DominioWebMapperSoporte::periodoDesdeDto).toList();
    }

    default TemporadaResponse aResponse(Temporada temporada) {
        return new TemporadaResponse(temporada.id().valor(), temporada.nombre(),
                temporada.periodos().stream().map(DominioWebMapperSoporte::periodoADto).toList(), temporada.esBase(),
                temporada.estanciaMinima().noches());
    }
}

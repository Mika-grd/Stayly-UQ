package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.PoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import org.mapstruct.Mapper;

import java.util.List;

/** DTO ↔ dominio del recurso Política de cancelación (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface PoliticaCancelacionWebMapper {

    default List<TramoPolitica> aTramos(PoliticaCancelacionRequest request) {
        return request.tramos().stream().map(DominioWebMapperSoporte::tramoDesdeDto).toList();
    }

    default PoliticaCancelacionResponse aResponse(PoliticaCancelacion politica) {
        return new PoliticaCancelacionResponse(politica.version().numero(), politica.vigenteDesde(),
                politica.tramos().stream().map(DominioWebMapperSoporte::tramoADto).toList(),
                politica.porcentajeRetencionNoShow());
    }
}

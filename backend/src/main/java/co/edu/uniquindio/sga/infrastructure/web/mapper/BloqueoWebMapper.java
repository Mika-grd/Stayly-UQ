package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import org.mapstruct.Mapper;

/** Dominio → DTO del recurso Bloqueo (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring")
public interface BloqueoWebMapper {

    default BloqueoResponse aResponse(Bloqueo bloqueo) {
        return new BloqueoResponse(bloqueo.id().valor(), bloqueo.apartamento().valor(), bloqueo.rango().inicio(),
                bloqueo.rango().fin(), bloqueo.motivo(), bloqueo.autor(), bloqueo.fechaRegistro());
    }
}

package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoDTO;
import co.edu.uniquindio.sga.application.dto.response.ParametrosReglasPropiasDTO;
import co.edu.uniquindio.sga.application.dto.response.ServicioAdicionalDTO;
import co.edu.uniquindio.sga.application.dto.response.UbicacionDTO;
import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.ServicioAdicional;
import org.mapstruct.Mapper;

/** Dominio → DTO del recurso Alojamiento (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface AlojamientoWebMapper {

    default ServicioAdicionalDTO aServicioDTO(ServicioAdicional servicio) {
        return new ServicioAdicionalDTO(servicio.nombre(), servicio.generaCargo(),
                servicio.generaCargo() ? DominioWebMapperSoporte.dineroALong(servicio.valor()) : null);
    }

    default ParametrosAlojamientoDTO aParametrosDTO(Alojamiento alojamiento) {
        return new ParametrosAlojamientoDTO(alojamiento.umbralEdad().anios(), alojamiento.tiempoPreparacion().horas(),
                alojamiento.plazoConfirmacion().horas(), alojamiento.horaLimiteNoShow().hora(),
                alojamiento.anticipo().exigido(),
                alojamiento.anticipo().exigido() ? alojamiento.anticipo().porcentaje() : null,
                alojamiento.mediosPago().stream().toList());
    }

    default ParametrosReglasPropiasDTO aReglasPropiasDTO(Alojamiento alojamiento) {
        return new ParametrosReglasPropiasDTO(alojamiento.cupoMascotas().maximo(),
                DominioWebMapperSoporte.dineroALong(alojamiento.cargoPorMascota().valorPorNoche()),
                alojamiento.recargoLlegadaNocturna().horaInicio(),
                DominioWebMapperSoporte.dineroALong(alojamiento.recargoLlegadaNocturna().valor()));
    }

    default AlojamientoResponse aResponse(Alojamiento alojamiento) {
        UbicacionDTO ubicacion = new UbicacionDTO(alojamiento.ubicacion().latitud(), alojamiento.ubicacion().longitud());
        return new AlojamientoResponse(alojamiento.nombre(), alojamiento.descripcion(), alojamiento.ciudad(),
                alojamiento.direccion(), ubicacion, alojamiento.horario().horaEntrada(),
                alojamiento.horario().horaSalida(), alojamiento.normasConvivencia(),
                alojamiento.servicios().stream().map(this::aServicioDTO).toList(), aParametrosDTO(alojamiento),
                aReglasPropiasDTO(alojamiento));
    }
}

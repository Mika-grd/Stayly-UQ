package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.AlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.request.ParametrosReglasPropiasRequest;
import co.edu.uniquindio.sga.application.usecase.ConfigurarAlojamientoUseCase;
import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.alojamiento.CargoPorMascota;
import co.edu.uniquindio.sga.domain.model.alojamiento.CupoMascotas;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.model.alojamiento.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.Ubicacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoDTO;
import co.edu.uniquindio.sga.application.dto.response.ParametrosReglasPropiasDTO;
import co.edu.uniquindio.sga.application.dto.response.ServicioAdicionalDTO;
import co.edu.uniquindio.sga.application.dto.response.UbicacionDTO;
import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.ServicioAdicional;
import org.mapstruct.Mapper;

import java.util.Set;

/** DTO ↔ dominio del recurso Alojamiento (sin lógica de negocio: solo cambia de forma). */
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

    default ServicioAdicional aServicio(ServicioAdicionalDTO dto) {
        return new ServicioAdicional(dto.nombre(), dto.generaCargo(),
                DominioWebMapperSoporte.dineroDesdeLong(dto.valor() == null ? 0L : dto.valor()));
    }

    /** Arma los objetos de valor; cada uno valida su rango del negocio al construirse (sección 6). */
    default ConfigurarAlojamientoUseCase.Comando aComando(AlojamientoRequest request) {
        ParametrosAlojamientoDTO parametros = request.parametros();
        boolean exigeAnticipo = parametros.exigeAnticipo();
        int porcentaje = exigeAnticipo && parametros.porcentajeAnticipo() != null ? parametros.porcentajeAnticipo() : 0;
        return new ConfigurarAlojamientoUseCase.Comando(request.nombre(), request.descripcion(), request.ciudad(),
                request.direccion(), new Ubicacion(request.ubicacion().latitud(), request.ubicacion().longitud()),
                request.normasConvivencia(), request.serviciosAdicionales().stream().map(this::aServicio).toList(),
                Set.copyOf(parametros.mediosPagoAceptados()),
                new HorarioAlojamiento(request.horaEntrada(), request.horaSalida()),
                new UmbralEdadFacturable(parametros.umbralEdadFacturable()),
                new TiempoPreparacion(parametros.tiempoPreparacionHoras()),
                new PlazoConfirmacion(parametros.plazoConfirmacionHoras()),
                new HoraLimiteNoShow(parametros.horaLimiteNoShow()), new Anticipo(exigeAnticipo, porcentaje));
    }

    default CupoMascotas aCupoMascotas(ParametrosReglasPropiasRequest request) {
        return new CupoMascotas(request.cupoMascotas());
    }

    default CargoPorMascota aCargoPorMascota(ParametrosReglasPropiasRequest request) {
        return new CargoPorMascota(DominioWebMapperSoporte.dineroDesdeLong(request.cargoPorMascotaNoche()));
    }

    default RecargoLlegadaNocturna aRecargoLlegadaNocturna(ParametrosReglasPropiasRequest request) {
        return new RecargoLlegadaNocturna(request.horaInicioLlegadaNocturna(),
                DominioWebMapperSoporte.dineroDesdeLong(request.valorRecargoLlegadaNocturna()));
    }
}

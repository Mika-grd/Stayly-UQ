package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.ApartamentoPorPrepararResponse;
import co.edu.uniquindio.sga.application.dto.response.CalendarioOcupacionResponse;
import co.edu.uniquindio.sga.application.dto.response.CalendarioOcupacionResponse.EstadoNoche;
import co.edu.uniquindio.sga.application.dto.response.CalendarioOcupacionResponse.NocheOcupacionDTO;
import co.edu.uniquindio.sga.application.dto.response.CalendarioOcupacionResponse.OcupacionApartamentoDTO;
import co.edu.uniquindio.sga.application.dto.response.EstadoOperativoResponse;
import co.edu.uniquindio.sga.application.dto.response.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaNovedadResponse;
import co.edu.uniquindio.sga.application.usecase.CambioEstadoOperativo;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase.Calendario;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase.NocheOcupacion;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase.OcupacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import org.mapstruct.Mapper;

/** Dominio → DTO de la operación diaria (7.6), sin lógica de negocio: solo cambia de forma. */
@Mapper(componentModel = "spring")
public interface OperacionDiariaWebMapper {

    default EstadoOperativoResponse aEstadoOperativo(CambioEstadoOperativo cambio) {
        Apartamento apartamento = cambio.apartamento();
        return new EstadoOperativoResponse(apartamento.identificacion().valor(), apartamento.estadoOperativo(),
                cambio.fecha(), cambio.autor());
    }

    default ApartamentoPorPrepararResponse aPorPreparar(Apartamento apartamento) {
        return new ApartamentoPorPrepararResponse(apartamento.identificacion().valor(), apartamento.nombre(),
                apartamento.estadoOperativo());
    }

    default NovedadResponse aNovedad(Novedad novedad) {
        return new NovedadResponse(novedad.id().valor(), novedad.apartamento().valor(), novedad.fecha(),
                novedad.autor(), novedad.descripcion(), novedad.gravedad());
    }

    default PaginaNovedadResponse aPaginaNovedades(Pagina<Novedad> pagina) {
        return new PaginaNovedadResponse(pagina.contenido().stream().map(this::aNovedad).toList(), pagina.numero(),
                pagina.tamano(), pagina.totalElementos(), pagina.totalPaginas());
    }

    default CalendarioOcupacionResponse aCalendario(Calendario calendario) {
        return new CalendarioOcupacionResponse(calendario.rango().inicio(), calendario.rango().fin(),
                calendario.apartamentos().stream().map(this::aOcupacion).toList());
    }

    default OcupacionApartamentoDTO aOcupacion(OcupacionApartamento ocupacion) {
        return new OcupacionApartamentoDTO(ocupacion.apartamento().valor(),
                ocupacion.noches().stream().map(this::aNoche).toList());
    }

    default NocheOcupacionDTO aNoche(NocheOcupacion noche) {
        return new NocheOcupacionDTO(noche.fecha(), EstadoNoche.valueOf(noche.estado().name()),
                noche.reserva() == null ? null : noche.reserva().valor());
    }
}

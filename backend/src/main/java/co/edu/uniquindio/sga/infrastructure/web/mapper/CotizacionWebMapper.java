package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.domain.model.reserva.Cotizacion;
import org.mapstruct.Mapper;

/** Dominio → DTO de la Cotización (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface CotizacionWebMapper {

    default CotizacionResponse aResponse(Cotizacion cotizacion) {
        Long cargoMascotas = cotizacion.cargoMascotas().esPositivo()
                ? DominioWebMapperSoporte.dineroALong(cotizacion.cargoMascotas()) : null;
        return new CotizacionResponse(cotizacion.apartamento().valor(), cotizacion.estancia().entrada(),
                cotizacion.estancia().salida(), cotizacion.estancia().noches(), cotizacion.ocupantesTotales(),
                cotizacion.ocupantesFacturables(),
                cotizacion.valor().noches().stream().map(DominioWebMapperSoporte::cargoNocheADto).toList(),
                DominioWebMapperSoporte.dineroALong(cotizacion.valor().total()), cargoMascotas,
                DominioWebMapperSoporte.dineroALong(cotizacion.total()));
    }
}

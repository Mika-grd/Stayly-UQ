package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.response.AutorizacionCierreDTO;
import co.edu.uniquindio.sga.application.dto.response.CargoDTO;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.dto.response.PagoDTO;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import org.mapstruct.Mapper;

import static co.edu.uniquindio.sga.infrastructure.web.mapper.DominioWebMapperSoporte.dineroALong;

/** Dominio → DTO del folio (7.7), sin lógica de negocio: el saldo ya lo calcula el agregado (F-1). */
@Mapper(componentModel = "spring")
public interface FolioWebMapper {

    default FolioResponse aFolioResponse(Folio folio) {
        AutorizacionCierreDTO autorizacion = folio.autorizacion()
                .map(a -> new AutorizacionCierreDTO(a.autor(), a.motivo(), a.fecha()))
                .orElse(null);
        return new FolioResponse(folio.id().valor(), folio.reserva().valor(), folio.estado(),
                folio.cargos().stream().map(this::aCargo).toList(),
                folio.pagos().stream().map(this::aPago).toList(),
                dineroALong(folio.totalCargos()), dineroALong(folio.totalPagos()),
                dineroALong(folio.saldo().valor()), autorizacion);
    }

    default CargoDTO aCargo(Cargo cargo) {
        return new CargoDTO(cargo.tipo(), cargo.concepto(), dineroALong(cargo.valor()), cargo.fecha());
    }

    default PagoDTO aPago(Pago pago) {
        return new PagoDTO(dineroALong(pago.valor()), pago.medio(), pago.fecha(), pago.referencia());
    }
}

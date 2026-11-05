package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.OcupanteRequest;
import co.edu.uniquindio.sga.application.dto.request.TitularRequest;
import co.edu.uniquindio.sga.application.dto.response.EstadoLiquidacion;
import co.edu.uniquindio.sga.application.dto.response.LiquidacionReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.OcupanteResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.ValorCongeladoDTO;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase.Comando;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase.DatosOcupante;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DTO ↔ dominio del recurso Reservas (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface ReservaWebMapper {

    default List<DatosOcupante> aDatosOcupantes(CrearReservaRequest request) {
        List<DatosOcupante> ocupantes = new ArrayList<>();
        TitularRequest titular = request.titular();
        ocupantes.add(new DatosOcupante(titular.nombre(), titular.fechaNacimiento(), titular.documentoIdentidad(),
                true, titular.correo(), titular.telefono()));
        if (request.acompanantes() != null) {
            for (OcupanteRequest acompanante : request.acompanantes()) {
                ocupantes.add(new DatosOcupante(acompanante.nombre(), acompanante.fechaNacimiento(),
                        acompanante.documentoIdentidad(), false, null, null));
            }
        }
        return ocupantes;
    }

    /** El canal y el autor los decide el controller según quién invoca; no viajan en el cuerpo. */
    default Comando aComando(CrearReservaRequest request, CanalOrigen canal, String autor) {
        return new Comando(new IdentificacionApartamento(request.identificacionApartamento()), request.fechaEntrada(),
                request.fechaSalida(), aDatosOcupantes(request), canal, null, request.horaEstimadaLlegada(),
                request.mascotasAutorizadas() == null ? 0 : request.mascotasAutorizadas(), autor);
    }

    default OcupanteResponse aOcupanteResponse(Ocupante ocupante, LocalDate fechaEntrada,
                                               UmbralEdadFacturable umbral) {
        return new OcupanteResponse(ocupante.id().valor(), ocupante.nombre(), ocupante.fechaNacimiento(),
                ocupante.esFacturableEn(fechaEntrada, umbral), ocupante.esTitular());
    }

    default ValorCongeladoDTO aValorCongeladoDTO(ValorCongelado valor) {
        return new ValorCongeladoDTO(valor.noches().stream().map(DominioWebMapperSoporte::cargoNocheADto).toList(),
                DominioWebMapperSoporte.dineroALong(valor.total()));
    }

    default ReservaResponse aReservaResponse(Reserva reserva, Folio folio, String advertenciaAcceso,
                                             UmbralEdadFacturable umbral) {
        List<OcupanteResponse> ocupantes = reserva.ocupantes().stream()
                .map(ocupante -> aOcupanteResponse(ocupante, reserva.estancia().entrada(), umbral))
                .toList();
        return new ReservaResponse(reserva.codigo().valor(), reserva.estado(), reserva.canal(),
                reserva.apartamento().valor(), advertenciaAcceso, reserva.estancia().entrada(),
                reserva.estancia().salida(), reserva.estancia().noches(),
                reserva.horaEstimadaLlegada().map(h -> h.hora()).orElse(null), ocupantes,
                reserva.mascotas().cantidad(), aValorCongeladoDTO(reserva.valor()),
                reserva.versionPolitica().numero(), DominioWebMapperSoporte.dineroALong(folio.saldo().valor()),
                reserva.fechaCreacion());
    }

    default ReservaResumenResponse aResumen(Reserva reserva) {
        return new ReservaResumenResponse(reserva.codigo().valor(), reserva.estado(), reserva.canal(),
                reserva.apartamento().valor(), reserva.estancia().entrada(), reserva.estancia().salida(),
                reserva.titular().nombre(), DominioWebMapperSoporte.dineroALong(reserva.valor().total()),
                reserva.fechaCreacion());
    }

    default LiquidacionReservaResponse aLiquidacion(Reserva reserva, Folio folio, EstadoLiquidacion estado) {
        long retencion = folio.cargos().stream()
                .filter(cargo -> cargo.tipo() == TipoCargo.PENALIDAD)
                .mapToLong(cargo -> DominioWebMapperSoporte.dineroALong(cargo.valor()))
                .sum();
        long saldo = DominioWebMapperSoporte.dineroALong(folio.saldo().valor());
        long devolucion = folio.saldo().esAFavor() ? -saldo : 0L;
        return new LiquidacionReservaResponse(reserva.codigo().valor(), estado, reserva.versionPolitica().numero(),
                retencion, devolucion, saldo);
    }
}

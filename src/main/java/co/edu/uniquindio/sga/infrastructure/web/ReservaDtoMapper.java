package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.response.LiquidacionReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Traduce entre los DTOs del contrato y los objetos del dominio de la reserva. No contiene reglas. */
@Component
public class ReservaDtoMapper {

    private final ConfiguracionAlojamiento configuracion;

    public ReservaDtoMapper(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public CrearReservaUseCase.Comando aComando(CrearReservaRequest request, CanalOrigen canal, String autor) {
        List<CrearReservaUseCase.DatosOcupante> ocupantes = new ArrayList<>();
        ocupantes.add(new CrearReservaUseCase.DatosOcupante(request.titular().nombre(),
                request.titular().fechaNacimiento(), request.titular().documentoIdentidad(), true,
                request.titular().correo(), request.titular().telefono()));
        request.acompanantes().forEach(acompanante -> ocupantes.add(new CrearReservaUseCase.DatosOcupante(
                acompanante.nombre(), acompanante.fechaNacimiento(), acompanante.documentoIdentidad(), false,
                null, null)));
        return new CrearReservaUseCase.Comando(new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(), request.fechaSalida(), ocupantes, canal, null,
                request.horaEstimadaLlegada(), request.mascotasAutorizadas(), autor);
    }

    public ReservaResponse aRespuesta(Reserva reserva, Folio folio, Optional<String> advertenciaAcceso) {
        List<ReservaResponse.OcupanteResponse> ocupantes = reserva.ocupantes().stream()
                .map(ocupante -> new ReservaResponse.OcupanteResponse(ocupante.id().valor(), ocupante.nombre(),
                        ocupante.fechaNacimiento(),
                        ocupante.esFacturableEn(reserva.estancia().entrada(), configuracion.umbralEdad()),
                        ocupante.esTitular()))
                .toList();
        List<ReservaResponse.CargoNocheResponse> noches = reserva.valor().noches().stream()
                .map(noche -> new ReservaResponse.CargoNocheResponse(noche.fecha(), noche.temporada(),
                        pesos(noche.tarifa()), noche.ocupantesFacturables(), pesos(noche.subtotal())))
                .toList();
        return new ReservaResponse(
                reserva.codigo().valor(),
                reserva.estado().name(),
                reserva.canal().name(),
                reserva.apartamento().valor(),
                advertenciaAcceso.orElse(null),
                reserva.estancia().entrada(),
                reserva.estancia().salida(),
                reserva.estancia().noches(),
                reserva.horaEstimadaLlegada().map(HoraEstimadaLlegada::hora).orElse(null),
                ocupantes,
                reserva.mascotas().cantidad(),
                new ReservaResponse.ValorCongeladoResponse(noches, pesos(reserva.valor().total())),
                reserva.versionPolitica().numero(),
                pesos(folio.saldo().valor()),
                reserva.fechaCreacion());
    }

    public ReservaResumenResponse aResumen(Reserva reserva) {
        return new ReservaResumenResponse(reserva.codigo().valor(), reserva.estado().name(), reserva.canal().name(),
                reserva.apartamento().valor(), reserva.estancia().entrada(), reserva.estancia().salida(),
                reserva.titular().nombre(), pesos(reserva.valor().total()), reserva.fechaCreacion());
    }

    public PaginaResponse<ReservaResumenResponse> aPagina(Pagina<Reserva> pagina) {
        return new PaginaResponse<>(pagina.contenido().stream().map(this::aResumen).toList(), pagina.numero(),
                pagina.tamano(), pagina.totalElementos(), pagina.totalPaginas());
    }

    /** Retención = penalidades del folio; devolución = lo pagado de más (saldo a favor). */
    public LiquidacionReservaResponse aLiquidacion(Reserva reserva, Folio folio) {
        long retencion = folio.cargos().stream()
                .filter(cargo -> cargo.tipo() == TipoCargo.PENALIDAD)
                .map(Cargo::valor)
                .reduce(Dinero.cero(), Dinero::sumar)
                .valor().longValue();
        long saldo = pesos(folio.saldo().valor());
        return new LiquidacionReservaResponse(reserva.codigo().valor(), reserva.estado().name(),
                reserva.versionPolitica().numero(), retencion, saldo < 0 ? -saldo : 0, saldo);
    }

    private static long pesos(Dinero dinero) {
        return dinero.valor().longValue();
    }
}

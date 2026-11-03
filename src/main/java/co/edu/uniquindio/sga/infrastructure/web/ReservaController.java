package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.response.EstadoLiquidacion;
import co.edu.uniquindio.sga.application.dto.response.LiquidacionReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaReservaResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDetalleApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.infrastructure.web.mapper.ReservaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

/**
 * Controller delgado del recurso Reserva: valida, traduce con el mapper y delega en el caso de
 * uso. Esta entrega no incluye JWT todavía, así que el canal y el autor quedan fijos hasta que la
 * seguridad (7.2) llegue en una entrega posterior.
 */
@RestController
public class ReservaController {

    private static final String AUTOR_PROVISIONAL = "Recepcion";

    private final CrearReservaUseCase crear;
    private final ConfirmarReservaUseCase confirmar;
    private final CancelarReservaUseCase cancelar;
    private final RegistrarLlegadaUseCase registrarLlegada;
    private final ConsultarReservaUseCase consultarReserva;
    private final ConsultarFolioUseCase consultarFolio;
    private final ConsultarDetalleApartamentoUseCase consultarApartamento;
    private final ConsultarReservasUseCase listar;
    private final ConfiguracionAlojamiento configuracion;
    private final ReservaWebMapper mapper;

    public ReservaController(CrearReservaUseCase crear, ConfirmarReservaUseCase confirmar,
                             CancelarReservaUseCase cancelar, RegistrarLlegadaUseCase registrarLlegada,
                             ConsultarReservaUseCase consultarReserva, ConsultarFolioUseCase consultarFolio,
                             ConsultarDetalleApartamentoUseCase consultarApartamento, ConsultarReservasUseCase listar,
                             ConfiguracionAlojamiento configuracion, ReservaWebMapper mapper) {
        this.crear = crear;
        this.confirmar = confirmar;
        this.cancelar = cancelar;
        this.registrarLlegada = registrarLlegada;
        this.consultarReserva = consultarReserva;
        this.consultarFolio = consultarFolio;
        this.consultarApartamento = consultarApartamento;
        this.listar = listar;
        this.configuracion = configuracion;
        this.mapper = mapper;
    }

    @PostMapping("/api/reservas")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest request,
                                                 UriComponentsBuilder uri) {
        var comando = mapper.aComando(request, CanalOrigen.PORTAL, AUTOR_PROVISIONAL);
        CrearReservaUseCase.ReservaCreada creada = crear.ejecutar(comando);
        ReservaResponse respuesta = mapper.aReservaResponse(creada.reserva(), creada.folio(),
                creada.advertenciaAcceso().orElse(null), configuracion.umbralEdad());
        var ubicacion = uri.path("/api/reservas/{codigo}").buildAndExpand(creada.reserva().codigo().valor()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping("/api/reservas/{codigo}")
    public ReservaResponse consultar(@PathVariable String codigo) {
        CodigoReserva id = new CodigoReserva(codigo);
        Reserva reserva = consultarReserva.ejecutar(id);
        Folio folio = consultarFolio.ejecutar(id);
        String advertencia = consultarApartamento.ejecutar(reserva.apartamento()).apartamento()
                .advertenciaAcceso().orElse(null);
        return mapper.aReservaResponse(reserva, folio, advertencia, configuracion.umbralEdad());
    }

    @GetMapping("/api/reservas")
    public PaginaReservaResumenResponse listar(@RequestParam(required = false) EstadoReserva estado,
                                               @RequestParam(required = false) CanalOrigen canal,
                                               @RequestParam(required = false) String apartamento,
                                               @RequestParam(required = false) String titular,
                                               @RequestParam(required = false) LocalDate desde,
                                               @RequestParam(required = false) LocalDate hasta,
                                               @RequestParam(defaultValue = "0") int pagina) {
        IdentificacionApartamento id = apartamento == null ? null : new IdentificacionApartamento(apartamento);
        FiltroReservas filtro = new FiltroReservas(estado, canal, id, titular, desde, hasta);
        Pagina<Reserva> resultado = listar.ejecutar(filtro, new PeticionPagina(pagina));
        var contenido = resultado.contenido().stream().map(mapper::aResumen).toList();
        return new PaginaReservaResumenResponse(contenido, resultado.numero(), resultado.tamano(),
                resultado.totalElementos(), resultado.totalPaginas());
    }

    @PutMapping("/api/reservas/{codigo}/confirmar")
    public ReservaResponse confirmar(@PathVariable String codigo) {
        confirmar.ejecutar(new CodigoReserva(codigo), AUTOR_PROVISIONAL);
        return consultar(codigo);
    }

    @PutMapping("/api/reservas/{codigo}/registrar-llegada")
    public ReservaResponse registrarLlegada(@PathVariable String codigo) {
        registrarLlegada.ejecutar(new CodigoReserva(codigo), AUTOR_PROVISIONAL);
        return consultar(codigo);
    }

    @PutMapping("/api/reservas/{codigo}/cancelar")
    public LiquidacionReservaResponse cancelar(@PathVariable String codigo,
                                               @RequestBody(required = false) CancelarReservaRequest request) {
        CodigoReserva id = new CodigoReserva(codigo);
        cancelar.ejecutar(id, AUTOR_PROVISIONAL);
        Reserva reserva = consultarReserva.ejecutar(id);
        Folio folio = consultarFolio.ejecutar(id);
        return mapper.aLiquidacion(reserva, folio, EstadoLiquidacion.CANCELADA);
    }
}

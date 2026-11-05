package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.response.ErrorNegocioResponse;
import co.edu.uniquindio.sga.application.dto.response.LiquidacionReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Adaptador REST de las reservas (Guía 12, 7.4). Cada endpoint aplica la matriz de roles de 7.1 con
 * {@code @PreAuthorize}; los que tienen dueño exigen además ser el titular ({@code @propietarioReserva}).
 * Use {@code hasAuthority}, no {@code hasRole}: los roles del token no llevan el prefijo ROLE_.
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Creación, consulta y transiciones de la reserva")
@ApiResponse(responseCode = "401", description = "Sin token, token inválido o vencido",
        content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
@ApiResponse(responseCode = "403", description = "El rol no lo permite o no es el titular de la reserva",
        content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
public class ReservaController {

    private static final String ROLES_RECEPCION = "hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')";
    private static final String RECEPCION_O_TITULAR = ROLES_RECEPCION
            + " or (hasAuthority('HUESPED') and @propietarioReserva.esTitular(#codigo, authentication))";

    private final CrearReservaUseCase crearReserva;
    private final ConsultarReservaUseCase consultarReserva;
    private final ConsultarReservasUseCase consultarReservas;
    private final ConfirmarReservaUseCase confirmarReserva;
    private final CancelarReservaUseCase cancelarReserva;
    private final ConsultarFolioUseCase consultarFolio;
    private final ReservaDtoMapper mapper;

    public ReservaController(CrearReservaUseCase crearReserva, ConsultarReservaUseCase consultarReserva,
                             ConsultarReservasUseCase consultarReservas, ConfirmarReservaUseCase confirmarReserva,
                             CancelarReservaUseCase cancelarReserva, ConsultarFolioUseCase consultarFolio,
                             ReservaDtoMapper mapper) {
        this.crearReserva = crearReserva;
        this.consultarReserva = consultarReserva;
        this.consultarReservas = consultarReservas;
        this.confirmarReserva = confirmarReserva;
        this.cancelarReserva = cancelarReserva;
        this.consultarFolio = consultarFolio;
        this.mapper = mapper;
    }

    /**
     * POST -> /api/reservas. Rol y canal (actividad 7.5.3): el huésped reserva solo por PORTAL y solo a
     * su nombre; recepción y administración, por DIRECTO. Es una regla de acceso, no del negocio: el
     * dominio solo sabe que la reserva tiene un canal y un titular, no quién usa el sistema (3.6).
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR')")
    @Operation(summary = "Crear reserva (CU-02)")
    @ApiResponse(responseCode = "201", description = "Reserva creada en estado PENDIENTE")
    public ResponseEntity<ReservaResponse> crearReserva(@Valid @RequestBody CrearReservaRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UsuarioAutenticado usuario = UsuarioAutenticado.de(jwt);
        CanalOrigen canal = usuario.esHuesped() ? CanalOrigen.PORTAL : CanalOrigen.DIRECTO;
        if (usuario.esHuesped() && !request.titular().documentoIdentidad().trim().equals(usuario.documento())) {
            throw new AccessDeniedException("Un huésped solo puede crear reservas a su nombre.");
        }
        CrearReservaUseCase.ReservaCreada creada =
                crearReserva.ejecutar(mapper.aComando(request, canal, usuario.correo()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.aRespuesta(creada.reserva(), creada.folio(), creada.advertenciaAcceso()));
    }

    /** GET -> /api/reservas/{codigo} */
    @GetMapping("/{codigo}")
    @PreAuthorize(RECEPCION_O_TITULAR)
    @Operation(summary = "Consultar reserva")
    @ApiResponse(responseCode = "200", description = "Detalle de la reserva")
    @ApiResponse(responseCode = "404", description = "La reserva no existe",
            content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable String codigo) {
        CodigoReserva codigoReserva = new CodigoReserva(codigo);
        Reserva reserva = consultarReserva.ejecutar(codigoReserva);
        Folio folio = consultarFolio.ejecutar(codigoReserva);
        return ResponseEntity.ok(mapper.aRespuesta(reserva, folio, Optional.empty()));
    }

    /** GET -> /api/reservas?estado=PENDIENTE */
    @GetMapping
    @PreAuthorize(ROLES_RECEPCION)
    @Operation(summary = "Consultar reservas", description = "Filtros opcionales, de a 10 por página")
    @ApiResponse(responseCode = "200", description = "Página de reservas")
    public ResponseEntity<PaginaResponse<ReservaResumenResponse>> listarReservas(
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) CanalOrigen canal,
            @RequestParam(required = false) String apartamento,
            @RequestParam(required = false) String titular,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina) {
        FiltroReservas filtro = new FiltroReservas(estado, canal,
                apartamento == null || apartamento.isBlank() ? null : new IdentificacionApartamento(apartamento),
                titular, desde, hasta);
        return ResponseEntity.ok(mapper.aPagina(consultarReservas.ejecutar(filtro, new PeticionPagina(pagina))));
    }

    /** PUT -> /api/reservas/{codigo}/confirmar */
    @PutMapping("/{codigo}/confirmar")
    @PreAuthorize(ROLES_RECEPCION)
    @Operation(summary = "Confirmar reserva (CU-03)")
    @ApiResponse(responseCode = "200", description = "Reserva confirmada")
    @ApiResponse(responseCode = "409", description = "El estado de la reserva no lo permite",
            content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
    public ResponseEntity<ReservaResponse> confirmarReserva(@PathVariable String codigo,
                                                            @AuthenticationPrincipal Jwt jwt) {
        CodigoReserva codigoReserva = new CodigoReserva(codigo);
        Reserva reserva = confirmarReserva.ejecutar(codigoReserva, UsuarioAutenticado.de(jwt).correo());
        return ResponseEntity.ok(mapper.aRespuesta(reserva, consultarFolio.ejecutar(codigoReserva),
                Optional.empty()));
    }

    /** PUT -> /api/reservas/{codigo}/cancelar */
    @PutMapping("/{codigo}/cancelar")
    @PreAuthorize(RECEPCION_O_TITULAR)
    @Operation(summary = "Cancelar reserva (CU-04)")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada con su liquidación")
    @ApiResponse(responseCode = "409", description = "El estado de la reserva no lo permite",
            content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
    public ResponseEntity<LiquidacionReservaResponse> cancelarReserva(
            @PathVariable String codigo,
            @RequestBody(required = false) CancelarReservaRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        CodigoReserva codigoReserva = new CodigoReserva(codigo);
        Folio folio = cancelarReserva.ejecutar(codigoReserva, UsuarioAutenticado.de(jwt).correo());
        return ResponseEntity.ok(mapper.aLiquidacion(consultarReserva.ejecutar(codigoReserva), folio));
    }
}

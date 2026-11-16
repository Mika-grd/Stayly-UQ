package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CerrarFolioRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarCargoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.usecase.CerrarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarCargoFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarPagoFolioUseCase;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.security.RolUsuario;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import co.edu.uniquindio.sga.infrastructure.web.mapper.FolioWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller delgado del folio de una reserva (7.7). El huésped solo consulta el de sus reservas
 * ({@code @propietarioReserva}); los movimientos y el cierre son de recepción. Cerrar con saldo exige
 * ser administrador (RN-17): el controller decide quién autoriza, el caso de uso aplica la regla.
 */
@RestController
public class FolioController {

    private static final String ROLES_RECEPCION = "hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')";
    private static final String RECEPCION_O_TITULAR = ROLES_RECEPCION
            + " or (hasAuthority('HUESPED') and @propietarioReserva.esTitular(#codigo, authentication))";

    private final ConsultarReservaUseCase consultarReserva;
    private final ConsultarFolioUseCase consultar;
    private final RegistrarPagoFolioUseCase registrarPago;
    private final RegistrarCargoFolioUseCase registrarCargo;
    private final CerrarFolioUseCase cerrar;
    private final FolioWebMapper mapper;

    public FolioController(ConsultarReservaUseCase consultarReserva, ConsultarFolioUseCase consultar,
                           RegistrarPagoFolioUseCase registrarPago, RegistrarCargoFolioUseCase registrarCargo,
                           CerrarFolioUseCase cerrar, FolioWebMapper mapper) {
        this.consultarReserva = consultarReserva;
        this.consultar = consultar;
        this.registrarPago = registrarPago;
        this.registrarCargo = registrarCargo;
        this.cerrar = cerrar;
        this.mapper = mapper;
    }

    /** CU-19: cargos, pagos y saldo calculado (RN-15). */
    @GetMapping("/api/reservas/{codigo}/folio")
    @PreAuthorize(RECEPCION_O_TITULAR)
    public FolioResponse consultar(@PathVariable String codigo) {
        CodigoReserva id = new CodigoReserva(codigo);
        consultarReserva.ejecutar(id);
        return mapper.aFolioResponse(consultar.ejecutar(id));
    }

    /** CU-07: abono con medio y fecha; admite pagos parciales. */
    @PostMapping("/api/reservas/{codigo}/folio/pagos")
    @PreAuthorize(ROLES_RECEPCION)
    public ResponseEntity<FolioResponse> registrarPago(@PathVariable String codigo,
                                                       @Valid @RequestBody RegistrarPagoRequest request) {
        Folio folio = registrarPago.ejecutar(new CodigoReserva(codigo), Dinero.pesos(request.valor()),
                request.medio(), request.fecha(), request.referencia());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.aFolioResponse(folio));
    }

    /** Cargo manual: SERVICIO_ADICIONAL o AJUSTE (RN-16). */
    @PostMapping("/api/reservas/{codigo}/folio/cargos")
    @PreAuthorize(ROLES_RECEPCION)
    public ResponseEntity<FolioResponse> registrarCargo(@PathVariable String codigo,
                                                        @Valid @RequestBody RegistrarCargoRequest request) {
        Folio folio = registrarCargo.ejecutar(new CodigoReserva(codigo), request.tipo().aTipoCargo(),
                request.concepto(), Dinero.pesos(request.valor()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.aFolioResponse(folio));
    }

    /** CU-18: con saldo cero cierra recepción; con saldo, solo el administrador y con motivo (RN-17). */
    @PutMapping("/api/reservas/{codigo}/folio/cerrar")
    @PreAuthorize(ROLES_RECEPCION)
    public FolioResponse cerrar(@PathVariable String codigo,
                                @Valid @RequestBody(required = false) CerrarFolioRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        UsuarioAutenticado usuario = UsuarioAutenticado.de(jwt);
        Optional<String> motivoAdministrador = usuario.tieneRol(RolUsuario.ADMINISTRADOR) && request != null
                ? Optional.ofNullable(request.motivoAutorizacion())
                : Optional.empty();
        return mapper.aFolioResponse(cerrar.ejecutar(new CodigoReserva(codigo), usuario.correo(), motivoAdministrador));
    }
}

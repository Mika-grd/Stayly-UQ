package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CambioContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.PerfilRequest;
import co.edu.uniquindio.sga.application.dto.request.UsuarioAdminRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaUsuarioResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.security.CuentaUsuarioService;
import co.edu.uniquindio.sga.application.security.GestionUsuariosService;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

/**
 * Controller delgado del recurso Usuario (7.2). La gestión es solo del administrador (7.1); el perfil
 * propio ({@code /me}) lo usa cualquier usuario autenticado y siempre sobre sí mismo: el correo sale
 * del token, nunca de la ruta.
 */
@RestController
public class UsuarioController {

    private static final String SOLO_ADMINISTRADOR = "hasAuthority('ADMINISTRADOR')";

    private final GestionUsuariosService gestion;
    private final CuentaUsuarioService cuenta;

    public UsuarioController(GestionUsuariosService gestion, CuentaUsuarioService cuenta) {
        this.gestion = gestion;
        this.cuenta = cuenta;
    }

    @GetMapping("/api/usuarios")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public PaginaUsuarioResponse listar(@RequestParam(defaultValue = "0") int pagina) {
        return gestion.listar(pagina);
    }

    @PostMapping("/api/usuarios")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioAdminRequest request,
                                                 UriComponentsBuilder uri) {
        UsuarioResponse creado = gestion.crear(request);
        return ResponseEntity.created(uri.path("/api/usuarios/{id}").buildAndExpand(creado.id()).toUri())
                .body(creado);
    }

    @GetMapping("/api/usuarios/{id}")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public UsuarioResponse consultar(@PathVariable UUID id) {
        return gestion.consultar(id);
    }

    @PutMapping("/api/usuarios/{id}")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public UsuarioResponse actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioAdminRequest request) {
        return gestion.actualizar(id, request);
    }

    /** Eliminación lógica (7.2): el usuario queda inactivo y ya no inicia sesión. */
    @DeleteMapping("/api/usuarios/{id}")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        gestion.eliminar(id, UsuarioAutenticado.de(jwt).correo());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/usuarios/me")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse consultarPerfil(@AuthenticationPrincipal Jwt jwt) {
        return cuenta.consultarPerfil(UsuarioAutenticado.de(jwt).correo());
    }

    @PutMapping("/api/usuarios/me")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse actualizarPerfil(@Valid @RequestBody PerfilRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return cuenta.actualizarPerfil(UsuarioAutenticado.de(jwt).correo(), request);
    }

    @PutMapping("/api/usuarios/me/contrasena")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambioContrasenaRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        cuenta.cambiarContrasena(UsuarioAutenticado.de(jwt).correo(), request);
        return ResponseEntity.noContent().build();
    }
}

package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.ConfirmarRecuperacionRequest;
import co.edu.uniquindio.sga.application.dto.request.SolicitudRecuperacionRequest;
import co.edu.uniquindio.sga.application.security.CuentaUsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Recuperación de contraseña con código por correo (7.2). Rutas públicas: quien la usa no tiene sesión. */
@RestController
@RequestMapping("/api/auth/recuperacion")
public class RecuperacionContrasenaController {

    private final CuentaUsuarioService cuenta;

    public RecuperacionContrasenaController(CuentaUsuarioService cuenta) {
        this.cuenta = cuenta;
    }

    /** Siempre 202: la respuesta no revela si el correo está registrado. */
    @PostMapping
    @SecurityRequirements
    public ResponseEntity<Void> solicitar(@Valid @RequestBody SolicitudRecuperacionRequest request) {
        cuenta.solicitarRecuperacion(request.correo());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirmar")
    @SecurityRequirements
    public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarRecuperacionRequest request) {
        cuenta.confirmarRecuperacion(request);
        return ResponseEntity.noContent().build();
    }
}

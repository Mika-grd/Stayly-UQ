package co.edu.uniquindio.sga.application.dto.response;

import java.util.UUID;

/** Usuario registrado (schema UsuarioResponse). Nunca incluye la contraseña ni su hash. */
public record UsuarioResponse(UUID id, String nombre, String correo, String telefono, String rol, boolean activo) {
}

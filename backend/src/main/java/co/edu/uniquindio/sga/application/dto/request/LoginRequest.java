package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada del inicio de sesión (Guía 12, schema LoginRequest del openapi.yaml). */
public record LoginRequest(
        @NotBlank(message = "es obligatorio")
        @Email(message = "no tiene un formato de correo válido")
        String correo,

        @NotBlank(message = "es obligatoria")
        String contrasena
) {
}

package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cambio voluntario de contraseña con la actual y la nueva (schema CambioContrasenaRequest, 7.2). */
public record CambioContrasenaRequest(
        @NotBlank String contrasenaActual,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "debe tener al menos 8 caracteres, con mayúsculas y números")
        String contrasenaNueva) {
}

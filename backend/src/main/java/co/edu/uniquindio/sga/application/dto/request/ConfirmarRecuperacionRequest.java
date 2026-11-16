package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Código recibido por correo y contraseña nueva (schema ConfirmarRecuperacionRequest, 7.2). */
public record ConfirmarRecuperacionRequest(
        @NotBlank @Email String correo,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "debe tener 6 dígitos") String codigo,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "debe tener al menos 8 caracteres, con mayúsculas y números")
        String contrasenaNueva) {
}

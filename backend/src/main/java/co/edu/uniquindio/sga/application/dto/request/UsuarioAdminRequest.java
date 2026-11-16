package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Usuario interno que gestiona el administrador (schema UsuarioAdminRequest, 7.2). La contraseña solo
 * se envía al crear; el correo no es editable después de creado.
 */
public record UsuarioAdminRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Size(max = 20) String telefono,
        @NotBlank @Size(max = 15) String documentoIdentidad,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotNull RolInterno rol,
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "debe tener al menos 8 caracteres, con mayúsculas y números")
        String contrasena) {
}

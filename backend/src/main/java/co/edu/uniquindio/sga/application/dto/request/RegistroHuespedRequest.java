package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cuerpo de POST /api/auth/registro (schema RegistroHuespedRequest, actividad 7.5.2). No trae rol: el
 * registro público siempre crea un HUESPED; los roles internos solo los asigna un administrador.
 */
public record RegistroHuespedRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "admite hasta 100 caracteres")
        String nombre,

        @NotBlank(message = "es obligatorio")
        @Email(message = "no tiene un formato de correo válido")
        @Size(max = 120, message = "admite hasta 120 caracteres")
        String correo,

        @NotBlank(message = "es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "debe tener al menos 8 caracteres, con mayúsculas y números")
        String contrasena,

        @NotBlank(message = "es obligatorio")
        @Size(max = 20, message = "admite hasta 20 caracteres")
        String telefono,

        @NotBlank(message = "es obligatorio")
        @Size(max = 15, message = "admite hasta 15 caracteres")
        String documentoIdentidad,

        @NotNull(message = "es obligatoria")
        @Past(message = "debe ser una fecha pasada")
        LocalDate fechaNacimiento
) {
}

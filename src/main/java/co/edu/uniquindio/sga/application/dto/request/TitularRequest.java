package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/** Titular de la reserva (schema TitularRequest): trae documento y datos de contacto. */
public record TitularRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotBlank(message = "es obligatorio") String documentoIdentidad,
        @NotNull(message = "es obligatoria") @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
        @NotBlank(message = "es obligatorio") @Email(message = "no tiene un formato de correo válido") String correo,
        @NotBlank(message = "es obligatorio") String telefono
) {
}

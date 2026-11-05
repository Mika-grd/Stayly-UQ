package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/** Acompañante de la reserva (schema OcupanteRequest): el documento es opcional. */
public record OcupanteRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotNull(message = "es obligatoria") @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
        String documentoIdentidad
) {
}

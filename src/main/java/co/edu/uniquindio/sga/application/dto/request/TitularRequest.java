package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Titular de la reserva (schema TitularRequest). */
public record TitularRequest(@NotBlank String nombre, @NotBlank String documentoIdentidad,
                             @NotNull LocalDate fechaNacimiento, @NotBlank @Email String correo,
                             @NotBlank String telefono) {
}

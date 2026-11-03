package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Acompañante del titular (schema OcupanteRequest); el documento es opcional para menores. */
public record OcupanteRequest(@NotBlank String nombre, @NotNull LocalDate fechaNacimiento, String documentoIdentidad) {
}

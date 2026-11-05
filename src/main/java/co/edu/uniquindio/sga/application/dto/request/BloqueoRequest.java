package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Bloqueo de noches de un apartamento (schema BloqueoRequest), [fechaInicio, fechaFin). */
public record BloqueoRequest(@NotNull LocalDate fechaInicio, @NotNull LocalDate fechaFin, @NotBlank String motivo) {
}

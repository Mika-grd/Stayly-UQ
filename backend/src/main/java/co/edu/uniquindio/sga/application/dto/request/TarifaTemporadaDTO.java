package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Tarifa a definir para una temporada (schema TarifaTemporadaDTO). */
public record TarifaTemporadaDTO(@NotNull UUID idTemporada, @Min(1) long valor) {
}

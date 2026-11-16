package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Tarifas a definir por temporada para un apartamento (schema TarifaRequest). */
public record TarifaRequest(@NotEmpty @Valid List<TarifaTemporadaDTO> tarifas) {
}

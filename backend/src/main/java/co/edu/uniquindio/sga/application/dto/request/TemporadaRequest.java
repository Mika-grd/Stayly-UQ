package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.dto.PeriodoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Datos de una temporada (schema TemporadaRequest). Vacío en periodos solo para la base. */
public record TemporadaRequest(@NotBlank String nombre, @Valid List<PeriodoDTO> periodos, boolean esBase,
                               @Min(1) Integer estanciaMinima) {
}

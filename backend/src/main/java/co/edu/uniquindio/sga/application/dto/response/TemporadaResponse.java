package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.application.dto.PeriodoDTO;

import java.util.List;
import java.util.UUID;

/** Temporada registrada (schema TemporadaResponse). */
public record TemporadaResponse(UUID id, String nombre, List<PeriodoDTO> periodos, boolean esBase,
                                int estanciaMinima) {
}

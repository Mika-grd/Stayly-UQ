package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;

/** Estancia mínima de una temporada, RP-01 (schema EstanciaMinimaRequest). */
public record EstanciaMinimaRequest(@Min(1) int estanciaMinima) {
}

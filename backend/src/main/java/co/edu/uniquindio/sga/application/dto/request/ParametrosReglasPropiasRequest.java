package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * Parámetros de las reglas propias RP-02 y RP-03 (schema ParametrosReglasPropiasRequest, CU-37). Solo
 * se valida la forma; cupo negativo o valores no positivos los rechaza el dominio (409).
 */
public record ParametrosReglasPropiasRequest(@NotNull Integer cupoMascotas,
                                             @NotNull Long cargoPorMascotaNoche,
                                             @NotNull LocalTime horaInicioLlegadaNocturna,
                                             @NotNull Long valorRecargoLlegadaNocturna) {
}

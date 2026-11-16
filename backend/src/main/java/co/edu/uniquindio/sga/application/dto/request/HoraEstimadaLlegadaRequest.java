package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/** Hora estimada de llegada, hora local de Bogotá (schema HoraEstimadaLlegadaRequest, CU-14, RN-09, RP-03). */
public record HoraEstimadaLlegadaRequest(@NotNull LocalTime horaEstimadaLlegada) {
}

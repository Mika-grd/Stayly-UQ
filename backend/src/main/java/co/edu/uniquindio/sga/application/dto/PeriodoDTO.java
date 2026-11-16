package co.edu.uniquindio.sga.application.dto;

import java.time.LocalDate;

/** Periodo de una temporada (schema PeriodoDTO), [fechaInicio, fechaFin). */
public record PeriodoDTO(LocalDate fechaInicio, LocalDate fechaFin) {
}

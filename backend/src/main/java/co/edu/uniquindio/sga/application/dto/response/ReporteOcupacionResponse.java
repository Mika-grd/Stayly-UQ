package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Reporte de ocupación (schema ReporteOcupacionResponse). {@code porcentajeOcupacion} es una proporción de 0 a 1. */
public record ReporteOcupacionResponse(LocalDate desde, LocalDate hasta, int apartamentosActivos, int nochesPeriodo,
                                       int nochesVendidas, double porcentajeOcupacion) {
}

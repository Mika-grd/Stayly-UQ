package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Reporte de reservas por canal (schema ReporteReservasPorCanalResponse). */
public record ReporteReservasPorCanalResponse(LocalDate desde, LocalDate hasta, List<ReservasCanalDTO> porCanal) {
}

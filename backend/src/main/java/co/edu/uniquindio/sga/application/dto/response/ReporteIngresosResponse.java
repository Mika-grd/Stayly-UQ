package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Reporte de ingresos (schema ReporteIngresosResponse): suma de pagos registrados, por medio. */
public record ReporteIngresosResponse(LocalDate desde, LocalDate hasta, long total, List<IngresoPorMedioDTO> porMedioPago) {
}

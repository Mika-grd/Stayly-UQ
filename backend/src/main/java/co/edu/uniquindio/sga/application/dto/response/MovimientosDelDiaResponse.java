package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Llegadas y salidas previstas de un día (schema MovimientosDelDiaResponse, CU-42). */
public record MovimientosDelDiaResponse(LocalDate fecha, List<ReservaResumenResponse> llegadas,
                                        List<ReservaResumenResponse> salidas) {
}

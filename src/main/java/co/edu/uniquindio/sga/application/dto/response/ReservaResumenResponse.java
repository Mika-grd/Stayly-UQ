package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fila del listado de reservas (schema ReservaResumenResponse). */
public record ReservaResumenResponse(String codigo, String estado, String canal, String identificacionApartamento,
                                     LocalDate fechaEntrada, LocalDate fechaSalida, String nombreTitular, long total,
                                     LocalDateTime fechaCreacion) {
}

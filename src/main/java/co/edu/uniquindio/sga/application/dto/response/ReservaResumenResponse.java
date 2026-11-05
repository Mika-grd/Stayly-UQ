package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Resumen de una reserva para listados (schema ReservaResumenResponse). */
public record ReservaResumenResponse(String codigo, EstadoReserva estado, CanalOrigen canal,
                                     String identificacionApartamento, LocalDate fechaEntrada, LocalDate fechaSalida,
                                     String nombreTitular, long total, LocalDateTime fechaCreacion) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Bloqueo registrado (schema BloqueoResponse). */
public record BloqueoResponse(UUID id, String identificacionApartamento, LocalDate fechaInicio, LocalDate fechaFin,
                              String motivo, String autor, LocalDateTime fechaRegistro) {
}

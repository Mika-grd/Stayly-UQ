package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Ocupante de la reserva (schema OcupanteResponse). */
public record OcupanteResponse(UUID id, String nombre, LocalDate fechaNacimiento, boolean facturable,
                               boolean titular) {
}

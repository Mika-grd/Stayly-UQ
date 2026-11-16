package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Ocupante de la reserva (schema OcupanteResponse). Documento y contacto solo los lleva el titular. */
public record OcupanteResponse(UUID id, String nombre, LocalDate fechaNacimiento, boolean facturable,
                               boolean titular, String documentoIdentidad, String correo, String telefono) {
}

package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;

import java.time.LocalDateTime;
import java.util.UUID;

/** Novedad registrada (schema NovedadResponse, CU-11). */
public record NovedadResponse(UUID id, String identificacionApartamento, LocalDateTime fecha, String autor,
                              String descripcion, GravedadNovedad gravedad) {
}

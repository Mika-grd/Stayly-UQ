package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Conflicto registrado con un canal externo (schema ConflictoCanalResponse, RN-18). */
public record ConflictoCanalResponse(UUID id, String canal, String identificadorExterno,
                                     String identificacionApartamento, LocalDate fechaEntrada, LocalDate fechaSalida,
                                     String codigoReservaVigente, EstadoConflicto estado, LocalDateTime fechaRegistro,
                                     String resolucion) {
}

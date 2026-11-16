package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tarifa con su vigencia (schema TarifaVigenteDTO). */
public record TarifaVigenteDTO(UUID idTemporada, String temporada, long valor, LocalDateTime vigenteDesde) {
}

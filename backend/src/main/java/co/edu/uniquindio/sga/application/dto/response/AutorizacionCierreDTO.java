package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;

/** Autorización de cierre con saldo (schema AutorizacionCierreDTO, RN-17). */
public record AutorizacionCierreDTO(String autor, String motivo, LocalDateTime fecha) {
}

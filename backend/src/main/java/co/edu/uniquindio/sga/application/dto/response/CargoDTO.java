package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;

import java.time.LocalDateTime;

/** Cargo del folio (schema CargoDTO). Negativo si es un movimiento inverso. */
public record CargoDTO(TipoCargo tipo, String concepto, long valor, LocalDateTime fecha) {
}

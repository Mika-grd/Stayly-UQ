package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;

import java.time.LocalDateTime;

/** Pago del folio (schema PagoDTO). Negativo si corrige un pago anterior. */
public record PagoDTO(long valor, MedioPago medio, LocalDateTime fecha, String referencia) {
}

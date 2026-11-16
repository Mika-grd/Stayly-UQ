package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Pago a registrar en el folio (schema RegistrarPagoRequest, CU-07, RN-15). Negativo solo para corregir. */
public record RegistrarPagoRequest(@NotNull Long valor, @NotNull MedioPago medio, @NotNull LocalDateTime fecha,
                                   @Size(max = 200) String referencia) {
}

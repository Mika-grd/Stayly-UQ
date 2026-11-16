package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cargo manual a registrar en el folio (schema RegistrarCargoRequest, RN-16). */
public record RegistrarCargoRequest(@NotNull TipoCargoManual tipo, @NotBlank @Size(max = 200) String concepto,
                                    @NotNull Long valor) {
}

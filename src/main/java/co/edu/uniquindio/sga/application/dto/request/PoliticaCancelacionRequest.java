package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.dto.TramoPoliticaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Nueva versión de la política de cancelación (schema PoliticaCancelacionRequest, L-10). */
public record PoliticaCancelacionRequest(@NotEmpty @Valid List<TramoPoliticaDTO> tramos,
                                         @Min(0) @Max(100) int porcentajeRetencionNoShow) {
}

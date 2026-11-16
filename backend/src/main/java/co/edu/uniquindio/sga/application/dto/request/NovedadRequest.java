package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Novedad del apartamento: daño, faltante o situación (schema NovedadRequest, CU-11). */
public record NovedadRequest(@NotBlank @Size(max = 500) String descripcion, @NotNull GravedadNovedad gravedad) {
}

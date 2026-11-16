package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Qué se hizo con el conflicto (schema ResolverConflictoRequest, 7.8). */
public record ResolverConflictoRequest(@NotBlank @Size(max = 1000) String resolucion) {
}

package co.edu.uniquindio.sga.application.dto.response;

import jakarta.validation.constraints.NotNull;

/** Coordenadas geográficas (schema UbicacionDTO). */
public record UbicacionDTO(@NotNull Double latitud, @NotNull Double longitud) {
}

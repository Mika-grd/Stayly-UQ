package co.edu.uniquindio.sga.application.dto.response;

import java.time.OffsetDateTime;

/**
 * Formato único de error de la API (schema ErrorNegocioResponse del openapi.yaml). El código HTTP
 * sigue el criterio de la hoja "Errores de Negocio": 400 formato, 404 no encontrado, 422 regla sobre
 * los datos y 409 conflicto con el estado actual. Nunca lleva una traza técnica.
 *
 * @param codigo  código estable de la hoja Errores de Negocio (p. ej. NOCHES_NO_DISPONIBLES)
 * @param mensaje mensaje en lenguaje del negocio
 * @param regla   regla violada (RN-XX, RP-XX o L-XX); {@code null} si el error no viene de una regla
 * @param ruta    endpoint invocado
 * @param fecha   momento del error, zona America/Bogota
 */
public record ErrorNegocioResponse(String codigo, String mensaje, String regla, String ruta, OffsetDateTime fecha) {
}

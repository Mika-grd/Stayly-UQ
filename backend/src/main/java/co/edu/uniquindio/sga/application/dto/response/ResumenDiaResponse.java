package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Resumen del día redactado por el modelo de IA (schema ResumenDiaResponse, 10.2). */
public record ResumenDiaResponse(LocalDate fecha, boolean disponible, String texto, String proveedor) {
}

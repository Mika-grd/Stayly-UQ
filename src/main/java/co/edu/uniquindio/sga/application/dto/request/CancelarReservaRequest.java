package co.edu.uniquindio.sga.application.dto.request;

/** Cuerpo de PUT /api/reservas/{codigo}/cancelar (schema CancelarReservaRequest). El motivo es opcional. */
public record CancelarReservaRequest(String motivo) {
}

package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Anotación del historial de la reserva: qué pasó, quién y cuándo. Nunca se modifica. */
public record EventoReserva(String descripcion, String autor, LocalDateTime fecha) {

    public EventoReserva {
        if (descripcion == null || descripcion.isBlank() || autor == null || autor.isBlank() || fecha == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El evento requiere descripción, autor y fecha.");
        }
    }
}

package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Horas que tiene una reserva PENDIENTE para confirmarse antes de vencer (L-14, RN-21). */
public record PlazoConfirmacion(int horas) {

    public PlazoConfirmacion {
        if (horas < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El plazo de confirmación no puede ser negativo.");
        }
    }

    public boolean vencido(LocalDateTime creacion, LocalDateTime ahora) {
        return !ahora.isBefore(creacion.plusHours(horas));
    }
}

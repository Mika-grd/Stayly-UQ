package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Horas de entrada y salida (L-12). La entrada es posterior a la salida del mismo día:
 * entre ambas queda la ventana de preparación (AL-1, 3.3).
 */
public record HorarioAlojamiento(LocalTime horaEntrada, LocalTime horaSalida) {

    public HorarioAlojamiento {
        if (horaEntrada == null || horaSalida == null || !horaEntrada.isAfter(horaSalida)) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La hora de entrada debe ser posterior a la hora de salida.");
        }
    }

    public long ventanaPreparacionHoras() {
        return Duration.between(horaSalida, horaEntrada).toHours();
    }
}

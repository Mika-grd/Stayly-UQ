package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Hora del día de entrada desde la que se puede declarar no-show (L-15). */
public record HoraLimiteNoShow(LocalTime hora) {

    public HoraLimiteNoShow {
        if (hora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La hora límite de no-show es obligatoria.");
        }
    }

    public boolean alcanzada(LocalDate entrada, LocalDateTime ahora) {
        return !ahora.isBefore(entrada.atTime(hora));
    }
}

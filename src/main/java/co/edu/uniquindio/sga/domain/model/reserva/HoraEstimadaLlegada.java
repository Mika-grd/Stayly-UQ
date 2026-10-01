package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalTime;

/** Hora estimada de llegada del grupo; obligatoria antes de confirmar (RN-09). */
public record HoraEstimadaLlegada(LocalTime hora) {

    public HoraEstimadaLlegada {
        if (hora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La hora estimada de llegada es obligatoria.");
        }
    }

    public boolean esIgualOPosteriorA(LocalTime referencia) {
        return !hora.isBefore(referencia);
    }
}

package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Horas que necesita un apartamento entre una salida y la siguiente llegada (L-13, RN-20). */
public record TiempoPreparacion(int horas) {

    public TiempoPreparacion {
        if (horas < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El tiempo de preparación no puede ser negativo.");
        }
    }

    /** true si la preparación cabe entre la hora de salida y la de entrada del mismo día. */
    public boolean cabeEn(HorarioAlojamiento horario) {
        return horas <= horario.ventanaPreparacionHoras();
    }
}

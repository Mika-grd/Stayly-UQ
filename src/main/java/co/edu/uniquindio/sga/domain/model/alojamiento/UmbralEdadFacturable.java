package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.Period;

/** Edad desde la que un ocupante es facturable (L-09, RN-06). Configuración del alojamiento. */
public record UmbralEdadFacturable(int anios) {

    public UmbralEdadFacturable {
        if (anios < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El umbral de edad facturable no puede ser negativo.");
        }
    }

    /** true si a la {@code fecha} la persona nacida en {@code nacimiento} ya cumplió el umbral. */
    public boolean alcanzadoPor(LocalDate nacimiento, LocalDate fecha) {
        return Period.between(nacimiento, fecha).getYears() >= anios;
    }
}

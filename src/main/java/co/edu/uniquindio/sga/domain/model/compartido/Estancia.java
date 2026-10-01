package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Rango [entrada, salida) de una reserva (3.1): la noche de la fecha de salida no se
 * ocupa ni se cobra. Toda estancia tiene al menos una noche (RN-03).
 */
public record Estancia(LocalDate entrada, LocalDate salida) {

    public Estancia {
        if (entrada == null || salida == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La estancia requiere fecha de entrada y de salida.");
        }
        if (!salida.isAfter(entrada)) {
            throw new ReglaDominioException("ESTANCIA_SIN_NOCHES",
                    "La fecha de salida debe ser posterior a la fecha de entrada.");
        }
    }

    public int noches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }

    /** Dos estancias se solapan si comparten al menos una noche (3.1). */
    public boolean seSolapaCon(Estancia otra) {
        return entrada.isBefore(otra.salida) && otra.entrada.isBefore(salida);
    }

    public boolean contiene(LocalDate noche) {
        return !noche.isBefore(entrada) && noche.isBefore(salida);
    }

    public List<LocalDate> fechasDeNoches() {
        return entrada.datesUntil(salida).toList();
    }
}

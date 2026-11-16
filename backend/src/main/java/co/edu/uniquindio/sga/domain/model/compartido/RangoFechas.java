package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Rango de noches [inicio, fin) de un bloqueo o de un periodo de temporada. Usa la misma
 * convención que {@link Estancia}: la fecha fin no queda incluida. Así, la temporada alta
 * "15 dic – 15 ene, ambas incluidas" se registra como [15-dic, 16-ene).
 */
public record RangoFechas(LocalDate inicio, LocalDate fin) {

    public RangoFechas {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new ReglaDominioException("RANGO_FECHAS_INVALIDO",
                    "La fecha final debe ser posterior a la fecha inicial.");
        }
    }

    public boolean seSolapaCon(Estancia estancia) {
        return inicio.isBefore(estancia.salida()) && estancia.entrada().isBefore(fin);
    }

    public boolean seSolapaCon(RangoFechas otro) {
        return inicio.isBefore(otro.fin) && otro.inicio.isBefore(fin);
    }

    public boolean contiene(LocalDate fecha) {
        return !fecha.isBefore(inicio) && fecha.isBefore(fin);
    }
}

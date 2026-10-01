package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Tope rígido de ocupantes del apartamento (F-03, RN-02, A-5). */
public record Capacidad(int personas) {

    public Capacidad {
        if (personas <= 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La capacidad debe ser un entero mayor que cero.");
        }
    }

    /** Todos los ocupantes cuentan, sean facturables o no (3.2). */
    public boolean admite(int totalOcupantes) {
        return totalOcupantes <= personas;
    }
}

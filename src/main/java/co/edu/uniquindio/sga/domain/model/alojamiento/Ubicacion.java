package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Coordenadas del alojamiento (L-02). No es dinero: double es adecuado. */
public record Ubicacion(double latitud, double longitud) {

    public Ubicacion {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La ubicación tiene coordenadas fuera de rango.");
        }
    }
}

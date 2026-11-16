package co.edu.uniquindio.sga.domain.model.politica;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Número de versión de la política; la reserva lo congela al crearse (RN-13, RN-22). */
public record VersionPolitica(int numero) {

    public VersionPolitica {
        if (numero < 1) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La versión de la política empieza en 1.");
        }
    }
}

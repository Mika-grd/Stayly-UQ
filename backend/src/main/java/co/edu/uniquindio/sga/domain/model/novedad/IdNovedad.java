package co.edu.uniquindio.sga.domain.model.novedad;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identificador de una novedad en el historial del apartamento. */
public record IdNovedad(UUID valor) {

    public IdNovedad {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador es obligatorio.");
        }
    }
}

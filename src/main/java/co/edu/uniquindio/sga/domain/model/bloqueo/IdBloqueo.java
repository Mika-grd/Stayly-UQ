package co.edu.uniquindio.sga.domain.model.bloqueo;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identificador de un bloqueo, para poder levantarlo. */
public record IdBloqueo(UUID valor) {

    public IdBloqueo {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador es obligatorio.");
        }
    }
}

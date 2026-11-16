package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identificador del conflicto que revisa el administrador. */
public record IdConflictoCanal(UUID valor) {

    public IdConflictoCanal {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador es obligatorio.");
        }
    }
}

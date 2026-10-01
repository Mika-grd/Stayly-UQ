package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identificador del canal; la reserva y el conflicto se refieren a él. */
public record IdCanal(UUID valor) {

    public IdCanal {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador es obligatorio.");
        }
    }
}

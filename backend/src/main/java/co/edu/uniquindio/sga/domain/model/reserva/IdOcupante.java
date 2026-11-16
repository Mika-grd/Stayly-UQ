package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identidad local de un ocupante dentro de su reserva. */
public record IdOcupante(UUID valor) {

    public IdOcupante {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador del ocupante es obligatorio.");
        }
    }
}

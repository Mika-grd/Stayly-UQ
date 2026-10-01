package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

public record IdFolio(UUID valor) {

    public IdFolio {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador del folio es obligatorio.");
        }
    }
}

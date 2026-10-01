package co.edu.uniquindio.sga.domain.model.temporada;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.UUID;

/** Identificador de la temporada; el Tarifario se refiere a ella sin contenerla. */
public record IdTemporada(UUID valor) {

    public IdTemporada {
        if (valor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador es obligatorio.");
        }
    }
}

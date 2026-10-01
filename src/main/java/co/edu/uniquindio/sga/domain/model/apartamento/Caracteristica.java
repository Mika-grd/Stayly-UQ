package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Dotación o característica (p. ej. "Balcón", "Cocina equipada"); se usa para filtrar (L-06). */
public record Caracteristica(String nombre) {

    public Caracteristica {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La característica debe tener nombre.");
        }
        nombre = nombre.trim();
    }
}

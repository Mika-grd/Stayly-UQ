package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Quién autorizó cerrar un folio con saldo, por qué y cuándo (RN-17). */
public record AutorizacionCierre(String autor, String motivo, LocalDateTime fecha) {

    public AutorizacionCierre {
        if (autor == null || autor.isBlank() || motivo == null || motivo.isBlank() || fecha == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La autorización de cierre requiere autor, motivo y fecha.");
        }
    }
}

package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Anotación de la bitácora del canal: qué se recibió y qué se respondió (7.8). */
public record EventoCanal(String tipo, String identificadorExterno, LocalDateTime fecha, String resultado) {

    public EventoCanal {
        if (tipo == null || tipo.isBlank() || fecha == null || resultado == null || resultado.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El evento del canal requiere tipo, fecha y resultado.");
        }
    }
}

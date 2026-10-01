package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;

/** Código que envía un canal externo. Canal + código es único (RN-19). */
public record IdentificadorExterno(IdCanal canal, String codigo) {

    public IdentificadorExterno {
        if (canal == null || codigo == null || codigo.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El identificador externo requiere canal y código.");
        }
        codigo = codigo.trim();
    }
}

package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Documento del ocupante: obligatorio para el titular, opcional para los demás. */
public record DocumentoIdentidad(String numero) {

    public DocumentoIdentidad {
        if (numero == null || numero.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El número de documento no puede estar vacío.");
        }
        numero = numero.trim();
    }
}

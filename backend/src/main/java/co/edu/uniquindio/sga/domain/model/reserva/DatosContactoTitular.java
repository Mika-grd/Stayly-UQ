package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Correo y teléfono del titular; van siempre juntos. */
public record DatosContactoTitular(String correo, String telefono) {

    public DatosContactoTitular {
        if (correo == null || !correo.contains("@")) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El correo del titular no es válido.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El teléfono del titular es obligatorio.");
        }
        correo = correo.trim();
        telefono = telefono.trim();
    }
}

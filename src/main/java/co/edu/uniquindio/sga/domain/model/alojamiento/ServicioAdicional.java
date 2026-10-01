package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

/** Servicio que ofrece el alojamiento y, si genera cargo, cuánto cuesta (L-17). */
public record ServicioAdicional(String nombre, boolean generaCargo, Dinero valor) {

    public ServicioAdicional {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El servicio adicional debe tener nombre.");
        }
        if (valor == null || (generaCargo && !valor.esPositivo())) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
    }
}

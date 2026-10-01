package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

/** Si se exige anticipo para confirmar y de qué porcentaje del valor del alojamiento (L-11). */
public record Anticipo(boolean exigido, int porcentaje) {

    public Anticipo {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El porcentaje de anticipo debe estar entre 0 y 100.");
        }
    }

    public Dinero exigidoSobre(Dinero valor) {
        return exigido ? valor.porcentaje(porcentaje).redondear() : Dinero.cero();
    }
}

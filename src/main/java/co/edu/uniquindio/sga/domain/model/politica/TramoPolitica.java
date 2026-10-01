package co.edu.uniquindio.sga.domain.model.politica;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Pareja antelación – retención: con {@code antelacionMinimaDias} o más días de antelación se
 * retiene {@code porcentajeRetencion} % del valor del alojamiento.
 */
public record TramoPolitica(int antelacionMinimaDias, int porcentajeRetencion) {

    public TramoPolitica {
        if (antelacionMinimaDias < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La antelación del tramo no puede ser negativa.");
        }
        if (porcentajeRetencion < 0 || porcentajeRetencion > 100) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La retención del tramo debe estar entre 0 % y 100 %.");
        }
    }
}

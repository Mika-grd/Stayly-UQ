package co.edu.uniquindio.sga.domain.model.temporada;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;

/** RP-01: noches mínimas que exige la temporada de la noche de entrada (T-2: al menos 1). */
public record EstanciaMinima(int noches) {

    public EstanciaMinima {
        if (noches < 1) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La estancia mínima debe ser de al menos 1 noche.");
        }
    }

    public boolean seCumpleCon(Estancia estancia) {
        return estancia.noches() >= noches;
    }
}

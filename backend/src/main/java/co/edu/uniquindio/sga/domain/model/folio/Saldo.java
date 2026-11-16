package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

/** Cargos menos pagos (RN-15). Se calcula, nunca se guarda. */
public record Saldo(Dinero valor) {

    public Saldo {
        if (valor == null) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
    }

    public boolean debeElHuesped() {
        return valor.esPositivo();
    }

    public boolean esAFavor() {
        return !valor.esPositivo() && !valor.esCero();
    }

    public boolean esCero() {
        return valor.esCero();
    }
}

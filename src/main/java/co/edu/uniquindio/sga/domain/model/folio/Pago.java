package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

import java.time.LocalDateTime;

/** Abono al folio. Todo pago tiene medio y fecha (F-4, RN-15); se corrige con su inverso (RN-16). */
public record Pago(Dinero valor, MedioPago medio, LocalDateTime fecha, String referencia) {

    public Pago {
        if (medio == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Todo pago debe indicar su medio de pago.");
        }
        if (fecha == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Todo pago debe indicar su fecha.");
        }
        if (valor == null || valor.redondear().esCero()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        valor = valor.redondear();
    }

    public Pago inverso(LocalDateTime ahora) {
        return new Pago(valor.negar(), medio, ahora, "Reverso" + (referencia == null ? "" : ": " + referencia));
    }
}

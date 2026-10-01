package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

import java.time.LocalDateTime;

/**
 * Cargo del folio. Nunca se modifica: se corrige con {@link #inverso(LocalDateTime)} (RN-16).
 * El valor se redondea al peso aquí, al final del cargo (3.4).
 */
public record Cargo(TipoCargo tipo, String concepto, Dinero valor, LocalDateTime fecha) {

    public Cargo {
        if (tipo == null || concepto == null || concepto.isBlank() || fecha == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El cargo requiere tipo, concepto y fecha.");
        }
        if (valor == null || valor.redondear().esCero()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        valor = valor.redondear();
    }

    public Cargo inverso(LocalDateTime ahora) {
        return new Cargo(tipo, "Anulación: " + concepto, valor.negar(), ahora);
    }
}

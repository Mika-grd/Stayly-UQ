package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.Locale;

/** Identificación única y estable del apartamento (L-04). "suq-201" y "SUQ-201" son la misma. */
public record IdentificacionApartamento(String valor) {

    public IdentificacionApartamento {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La identificación del apartamento es obligatoria.");
        }
        valor = valor.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return valor;
    }
}

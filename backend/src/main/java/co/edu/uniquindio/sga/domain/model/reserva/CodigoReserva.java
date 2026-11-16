package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Identificador de la reserva, p. ej. RES-2026-00042. */
public record CodigoReserva(String valor) {

    public CodigoReserva {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El código de reserva es obligatorio.");
        }
        valor = valor.trim();
    }

    @Override
    public String toString() {
        return valor;
    }
}

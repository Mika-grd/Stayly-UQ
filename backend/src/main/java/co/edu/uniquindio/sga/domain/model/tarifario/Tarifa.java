package co.edu.uniquindio.sga.domain.model.tarifario;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;

import java.time.LocalDateTime;

/** Valor por ocupante facturable por noche en una temporada, desde una fecha (TA-1: positivo en COP). */
public record Tarifa(IdTemporada temporada, Dinero valor, LocalDateTime vigenteDesde) {

    public Tarifa {
        if (temporada == null || vigenteDesde == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La tarifa requiere temporada y fecha de vigencia.");
        }
        if (valor == null || !valor.esPositivo()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        valor = valor.redondear();
    }
}

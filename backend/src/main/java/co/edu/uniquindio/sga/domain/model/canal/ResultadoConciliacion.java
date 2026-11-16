package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;

/**
 * Lo que devuelve ConciliacionCanalExternoService: ACEPTAR (seguir el flujo de creación),
 * YA_EXISTE (con la reserva ya creada) o CONFLICTO (con el ConflictoCanal nuevo). No se guarda.
 */
public record ResultadoConciliacion(TipoConciliacion tipo, CodigoReserva reservaExistente, ConflictoCanal conflicto) {

    public ResultadoConciliacion {
        boolean coherente = switch (tipo) {
            case ACEPTAR -> reservaExistente == null && conflicto == null;
            case YA_EXISTE -> reservaExistente != null && conflicto == null;
            case CONFLICTO -> reservaExistente == null && conflicto != null;
            case null -> false;
        };
        if (!coherente) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El resultado de la conciliación no es coherente.");
        }
    }

    public static ResultadoConciliacion aceptar() {
        return new ResultadoConciliacion(TipoConciliacion.ACEPTAR, null, null);
    }

    public static ResultadoConciliacion yaExiste(CodigoReserva codigo) {
        return new ResultadoConciliacion(TipoConciliacion.YA_EXISTE, codigo, null);
    }

    public static ResultadoConciliacion conflicto(ConflictoCanal conflicto) {
        return new ResultadoConciliacion(TipoConciliacion.CONFLICTO, null, conflicto);
    }
}

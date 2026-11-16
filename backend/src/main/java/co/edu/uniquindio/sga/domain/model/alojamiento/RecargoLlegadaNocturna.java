package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * RP-03: si la hora estimada de llegada es igual o posterior a {@code horaInicio} se registra
 * un recargo en el folio, una sola vez por reserva. Es una regla sobre valores, por eso es un
 * objeto de valor y no un servicio.
 */
public record RecargoLlegadaNocturna(LocalTime horaInicio, Dinero valor) {

    static final String CONCEPTO = "Recargo por llegada nocturna";

    public RecargoLlegadaNocturna {
        if (horaInicio == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La hora de inicio del recargo nocturno es obligatoria.");
        }
        if (valor == null || !valor.esPositivo()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
    }

    public boolean aplicaA(HoraEstimadaLlegada hora) {
        return hora != null && hora.esIgualOPosteriorA(horaInicio);
    }

    /**
     * Compara la hora anterior (null si no había) con la nueva: si se vuelve nocturna devuelve
     * el recargo; si deja de serlo, su movimiento inverso; si no cambia de condición, nada.
     * Así nunca queda más de un recargo vigente.
     */
    public Optional<Cargo> cargoPorCambioDeHora(HoraEstimadaLlegada anterior, HoraEstimadaLlegada nueva,
                                                LocalDateTime ahora) {
        boolean eraNocturna = aplicaA(anterior);
        boolean esNocturna = aplicaA(nueva);
        if (esNocturna == eraNocturna) {
            return Optional.empty();
        }
        Cargo recargo = new Cargo(TipoCargo.SERVICIO_ADICIONAL, CONCEPTO, valor, ahora);
        return Optional.of(esNocturna ? recargo : recargo.inverso(ahora));
    }
}

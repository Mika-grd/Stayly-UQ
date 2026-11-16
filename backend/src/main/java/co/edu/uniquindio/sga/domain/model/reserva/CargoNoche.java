package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

import java.time.LocalDate;

/** Línea del desglose: la noche, su temporada, la tarifa y cuántos pagan (RN-05). */
public record CargoNoche(LocalDate fecha, String temporada, Dinero tarifa, int ocupantesFacturables) {

    public CargoNoche {
        if (fecha == null || temporada == null || temporada.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La noche requiere fecha y temporada.");
        }
        if (tarifa == null || !tarifa.esPositivo() || ocupantesFacturables < 0) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
    }

    /** Sin redondear: el redondeo se aplica al total del cargo, nunca noche por noche (3.4). */
    public Dinero subtotal() {
        return tarifa.multiplicar(ocupantesFacturables);
    }
}

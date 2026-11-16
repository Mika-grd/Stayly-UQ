package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

import java.util.List;
import java.util.Objects;

/** Precio de la estancia con su detalle noche por noche, fijado al crear la reserva (RN-22). */
public record ValorCongelado(List<CargoNoche> noches) {

    public ValorCongelado {
        if (noches == null || noches.isEmpty() || noches.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El valor congelado requiere al menos una noche.");
        }
        noches = List.copyOf(noches);
    }

    public Dinero total() {
        return noches.stream()
                .map(CargoNoche::subtotal)
                .reduce(Dinero.cero(), Dinero::sumar)
                .redondear();
    }

    /** Positiva si el nuevo valor es mayor (el huésped debe más), negativa si es menor. */
    public Dinero diferenciaCon(ValorCongelado anterior) {
        return total().restar(anterior.total());
    }
}

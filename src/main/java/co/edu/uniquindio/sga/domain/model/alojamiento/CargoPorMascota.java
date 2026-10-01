package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;

/** Valor por mascota autorizada y por noche (RP-02). */
public record CargoPorMascota(Dinero valorPorNoche) {

    public CargoPorMascota {
        if (valorPorNoche == null || !valorPorNoche.esPositivo()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
    }

    /** mascotas × noches × valor por noche. */
    public Dinero calcular(MascotasAutorizadas mascotas, Estancia estancia) {
        return valorPorNoche.multiplicar(mascotas.cantidad()).multiplicar(estancia.noches()).redondear();
    }
}

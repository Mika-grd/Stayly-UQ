package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Cantidad de mascotas declaradas en la reserva (RP-02). No cuentan para la capacidad ni son ocupantes. */
public record MascotasAutorizadas(int cantidad) {

    public MascotasAutorizadas {
        if (cantidad < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La cantidad de mascotas no puede ser negativa.");
        }
    }

    public static MascotasAutorizadas ninguna() {
        return new MascotasAutorizadas(0);
    }

    public boolean hayMascotas() {
        return cantidad > 0;
    }
}

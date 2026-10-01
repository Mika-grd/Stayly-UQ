package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;

/** Número máximo de mascotas autorizadas por reserva (RP-02). */
public record CupoMascotas(int maximo) {

    public CupoMascotas {
        if (maximo < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El cupo de mascotas no puede ser negativo.");
        }
    }

    public boolean permite(MascotasAutorizadas mascotas) {
        return mascotas.cantidad() <= maximo;
    }
}

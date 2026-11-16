package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;

/**
 * RP-02: depende de una característica del Apartamento y del cupo configurable del alojamiento;
 * la Reserva no conoce ninguno de los dos.
 */
public class AdmisionMascotasService {

    private final ConfiguracionAlojamiento configuracion;

    public AdmisionMascotasService(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public void verificarMascotas(Apartamento apartamento, MascotasAutorizadas mascotas) {
        if (!mascotas.hayMascotas()) {
            return;
        }
        if (!apartamento.admiteMascotas()) {
            throw new ReglaDominioException("MASCOTAS_NO_ADMITIDAS", "Este apartamento no admite mascotas.");
        }
        if (!configuracion.cupoMascotas().permite(mascotas)) {
            throw new ReglaDominioException("CUPO_MASCOTAS_EXCEDIDO",
                    "Se admiten como máximo " + configuracion.cupoMascotas().maximo() + " mascotas por reserva.");
        }
    }

    public Dinero cargoPorMascotas(MascotasAutorizadas mascotas, Estancia estancia) {
        return configuracion.cargoPorMascota().calcular(mascotas, estancia);
    }
}

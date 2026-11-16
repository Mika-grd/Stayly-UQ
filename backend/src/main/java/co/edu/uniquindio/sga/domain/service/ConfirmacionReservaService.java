package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;

import java.time.LocalDateTime;

/**
 * L-11: el anticipo pagado está en el Folio, que la Reserva no puede ver. Primero la Reserva verifica
 * su estado y su hora de llegada (RN-08, RN-09), después se verifica el anticipo y al final
 * Reserva.confirmar hace la transición.
 */
public class ConfirmacionReservaService {

    private final ConfiguracionAlojamiento configuracion;

    public ConfirmacionReservaService(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public void confirmar(Reserva reserva, Folio folio, String autor, LocalDateTime ahora) {
        if (!folio.reserva().equals(reserva.codigo())) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El folio no corresponde a la reserva.");
        }
        reserva.verificarConfirmable();
        Anticipo anticipo = configuracion.anticipo();
        Dinero faltante = anticipo.exigidoSobre(reserva.valor().total()).restar(folio.totalPagos());
        if (faltante.esPositivo()) {
            throw new ReglaDominioException("ANTICIPO_INSUFICIENTE",
                    "Para confirmar se requiere un anticipo del " + anticipo.porcentaje()
                            + " % del valor de la reserva; faltan " + faltante + ".");
        }
        reserva.confirmar(autor, ahora);
    }
}

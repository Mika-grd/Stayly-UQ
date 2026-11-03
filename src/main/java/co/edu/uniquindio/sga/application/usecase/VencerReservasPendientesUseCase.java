package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CU-17 (RN-21): las reservas PENDIENTE que superaron el plazo de confirmación pasan a CANCELADA
 * y liberan sus noches (RN-12), sin intervención manual. Reserva.vencer decide si el plazo se
 * cumplió. El folio anula el alojamiento sin retención: la reserva nunca quedó en firme.
 */
@Service
@Transactional
public class VencerReservasPendientesUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public VencerReservasPendientesUseCase(ReservaRepository reservas, FolioRepository folios,
                                           ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    /** Devuelve los códigos de las reservas que vencieron en esta pasada. */
    public List<CodigoReserva> ejecutar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        PlazoConfirmacion plazo = configuracion.plazoConfirmacion();
        List<Reserva> candidatas = reservas.buscarPendientesCreadasAntesDe(ahora.minusHours(plazo.horas()));
        for (Reserva reserva : candidatas) {
            reserva.vencer(plazo, ahora);
            folios.buscarPorCodigoReserva(reserva.codigo()).ifPresent(folio -> {
                folio.liquidarCancelacion(Dinero.cero(), ahora);
                folios.guardar(folio);
            });
            reservas.guardar(reserva);
        }
        return candidatas.stream().map(Reserva::codigo).toList();
    }
}

package co.edu.uniquindio.sga.infrastructure.programacion;

import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RN-21 y 7.5: el vencimiento de las reservas pendientes ocurre sin intervención manual. Cada
 * {@code sga.vencimiento.intervalo} (por defecto 5 minutos) ejecuta CU-17. Se apaga con
 * {@code sga.vencimiento.habilitado=false}.
 */
@Component
@ConditionalOnProperty(name = "sga.vencimiento.habilitado", havingValue = "true", matchIfMissing = true)
public class VencimientoReservasProgramado {

    private static final Logger LOG = LoggerFactory.getLogger(VencimientoReservasProgramado.class);

    private final VencerReservasPendientesUseCase vencerReservas;

    public VencimientoReservasProgramado(VencerReservasPendientesUseCase vencerReservas) {
        this.vencerReservas = vencerReservas;
    }

    @Scheduled(initialDelayString = "${sga.vencimiento.retraso-inicial:PT1M}",
            fixedDelayString = "${sga.vencimiento.intervalo:PT5M}")
    public void vencerPendientes() {
        List<CodigoReserva> vencidas = vencerReservas.ejecutar();
        if (!vencidas.isEmpty()) {
            LOG.info("Reservas PENDIENTE vencidas por el plazo de confirmación: {}", vencidas);
        }
    }
}

package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.NotificadorTitular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;

/**
 * Adaptador del aviso al titular por correo (10.3.2 opción A). El texto se arma en el hilo de la
 * operación, pero el envío se hace después del commit y en un hilo aparte: si la operación se
 * revierte no se avisa nada, y un proveedor lento o caído no demora ni rompe la reserva (I-05). Un
 * fallo queda en el registro de la aplicación y la operación sigue (I-04). El canal concreto (buzón
 * simulado o SMTP) lo decide el {@link NotificadorCorreo} configurado. Se apaga con
 * {@code sga.notificaciones.habilitadas=false}, sin tocar código.
 */
@Component
@ConditionalOnProperty(name = "sga.notificaciones.habilitadas", havingValue = "true", matchIfMissing = true)
public class NotificadorTitularCorreo implements NotificadorTitular {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorTitularCorreo.class);

    private final NotificadorCorreo correo;
    private final ConfiguracionAlojamiento configuracion;
    private final Executor ejecutor;

    public NotificadorTitularCorreo(NotificadorCorreo correo, ConfiguracionAlojamiento configuracion,
                                    @Qualifier("ejecutorNotificaciones") Executor ejecutor) {
        this.correo = correo;
        this.configuracion = configuracion;
        this.ejecutor = ejecutor;
    }

    @Override
    public void notificar(Reserva reserva, Motivo motivo) {
        try {
            var contacto = reserva.titular().contacto();
            if (contacto.isEmpty()) {
                return;
            }
            String destinatario = contacto.get().correo();
            String alojamiento = configuracion.alojamiento().nombre();
            String asunto = RedaccionAvisoReserva.asunto(alojamiento, reserva, motivo);
            String cuerpo = RedaccionAvisoReserva.cuerpo(alojamiento, reserva, motivo);
            despuesDelCommit(() -> enviar(destinatario, asunto, cuerpo, reserva, motivo));
        } catch (RuntimeException e) {
            LOG.warn("No se pudo preparar el aviso {} de {}: {}", motivo, reserva.codigo().valor(), e.getMessage());
        }
    }

    private void enviar(String destinatario, String asunto, String cuerpo, Reserva reserva, Motivo motivo) {
        try {
            ejecutor.execute(() -> {
                try {
                    correo.enviar(destinatario, asunto, cuerpo);
                } catch (RuntimeException e) {
                    LOG.warn("Aviso {} de {} no enviado: {}", motivo, reserva.codigo().valor(), e.getMessage());
                }
            });
        } catch (RuntimeException e) {
            LOG.warn("Cola de avisos llena; se omite el aviso {} de {}", motivo, reserva.codigo().valor());
        }
    }

    private static void despuesDelCommit(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}

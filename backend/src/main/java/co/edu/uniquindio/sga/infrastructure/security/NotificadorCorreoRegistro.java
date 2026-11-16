package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Buzón simulado: escribe el correo en el log de la aplicación en lugar de enviarlo. Sirve para la
 * demostración sin servidor SMTP; un adaptador real implementaría el mismo puerto.
 */
@Component
public class NotificadorCorreoRegistro implements NotificadorCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorCorreoRegistro.class);

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        LOG.info("Correo simulado para {} · {} · {}", destinatario, asunto, cuerpo);
    }
}

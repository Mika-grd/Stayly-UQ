package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Buzón simulado: escribe el correo en el log de la aplicación en lugar de enviarlo. Es la
 * alternativa local de I-02 (sin internet ni credenciales) y la que usan las pruebas (I-08); con
 * {@code sga.correo.proveedor=smtp} la reemplaza NotificadorCorreoSmtp.
 */
@Component
@ConditionalOnProperty(name = "sga.correo.proveedor", havingValue = "registro", matchIfMissing = true)
public class NotificadorCorreoRegistro implements NotificadorCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorCorreoRegistro.class);

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        LOG.info("Correo simulado para {} · {} · {}", destinatario, asunto, cuerpo);
    }
}

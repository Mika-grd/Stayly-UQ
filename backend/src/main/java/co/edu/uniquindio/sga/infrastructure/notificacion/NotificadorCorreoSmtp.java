package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Correo real por SMTP (10.3.2 opción A): un servidor con clave de aplicación, un servicio
 * transaccional con nivel gratuito o un buzón de captura para pruebas. Se activa con
 * {@code sga.correo.proveedor=smtp}; servidor, usuario y clave llegan por variables de entorno
 * (I-06) y los tiempos de espera están en {@code spring.mail.properties} (I-03). Un fallo de envío
 * se registra y no se propaga (I-04).
 */
@Component
@ConditionalOnProperty(name = "sga.correo.proveedor", havingValue = "smtp")
public class NotificadorCorreoSmtp implements NotificadorCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorCorreoSmtp.class);

    private final JavaMailSender servidor;
    private final String remitente;

    public NotificadorCorreoSmtp(JavaMailSender servidor, @Value("${sga.correo.remitente}") String remitente) {
        this.servidor = servidor;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            servidor.send(mensaje);
            LOG.info("Correo enviado a {}: {}", destinatario, asunto);
        } catch (MailException e) {
            LOG.warn("No se pudo enviar el correo a {} ({}): {}", destinatario, asunto, e.getMessage());
        }
    }
}

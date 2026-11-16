package co.edu.uniquindio.sga.application.security;

/** Puerto de salida para enviar correos (7.2: código de recuperación de contraseña). */
public interface NotificadorCorreo {

    void enviar(String destinatario, String asunto, String cuerpo);
}

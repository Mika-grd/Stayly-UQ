package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.CambioContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.ConfirmarRecuperacionRequest;
import co.edu.uniquindio.sga.application.dto.request.PerfilRequest;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;

/** CU-39 y CU-40 (7.2): perfil propio, cambio de contraseña y recuperación con código por correo. */
public interface CuentaUsuarioService {

    UsuarioResponse consultarPerfil(String correo);

    UsuarioResponse actualizarPerfil(String correo, PerfilRequest request);

    void cambiarContrasena(String correo, CambioContrasenaRequest request);

    /** Envía un código de 6 dígitos, vigente 15 minutos. No revela si el correo existe. */
    void solicitarRecuperacion(String correo);

    void confirmarRecuperacion(ConfirmarRecuperacionRequest request);
}

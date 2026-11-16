package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.UsuarioAdminRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaUsuarioResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;

import java.util.UUID;

/**
 * CU-38: el administrador gestiona usuarios y roles (7.1, 7.2). Los usuarios son un concepto de
 * acceso, no del negocio: no tienen agregado en el dominio (3.6).
 */
public interface GestionUsuariosService {

    PaginaUsuarioResponse listar(int pagina);

    UsuarioResponse crear(UsuarioAdminRequest request);

    UsuarioResponse consultar(UUID id);

    UsuarioResponse actualizar(UUID id, UsuarioAdminRequest request);

    /** Eliminación lógica; nunca si es titular de reservas PENDIENTE, CONFIRMADA o EN_CURSO (7.2). */
    void eliminar(UUID id, String correoAdministrador);
}

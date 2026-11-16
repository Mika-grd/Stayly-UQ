package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;

/** Entidad de usuario → UsuarioResponse, sin la contraseña ni su hash. */
final class UsuarioRespuestas {

    private UsuarioRespuestas() {
    }

    static UsuarioResponse de(UsuarioEntity usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getTelefono(),
                usuario.getRol().name(), usuario.isActivo(), usuario.getFotoUrl());
    }
}

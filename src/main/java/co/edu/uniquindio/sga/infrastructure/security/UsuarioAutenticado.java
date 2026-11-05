package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Lo que el controller necesita saber del usuario del token: correo (autor de los eventos), documento
 * (puente con el titular, 3.6) y roles. Se lee del JWT ya validado, sin consultar la base de datos.
 */
public record UsuarioAutenticado(String correo, String documento, List<String> roles) {

    public static UsuarioAutenticado de(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new UsuarioAutenticado(jwt.getSubject(), jwt.getClaimAsString("documento"),
                roles == null ? List.of() : roles);
    }

    public boolean tieneRol(RolUsuario rol) {
        return roles.contains(rol.name());
    }

    public boolean esHuesped() {
        return tieneRol(RolUsuario.HUESPED);
    }
}

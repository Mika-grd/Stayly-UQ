package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.infrastructure.security.RolUsuario;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.util.List;

/**
 * Usuarios iniciales leídos de application.properties (Guía 12, 12.3). La registra el
 * {@code @ConfigurationPropertiesScan} de {@code SgaApplication}.
 */
@ConfigurationProperties(prefix = "sga.usuarios-iniciales")
public record DefaultUserProperties(List<DefaultUser> usuarios) {

    public DefaultUserProperties {
        usuarios = usuarios == null ? List.of() : List.copyOf(usuarios);
    }

    public record DefaultUser(String nombre, String correo, String contrasena, String telefono, String documento,
                              LocalDate fechaNacimiento, RolUsuario rol) {
    }
}

package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

/**
 * Carga los usuarios iniciales al arrancar (Guía 12, 12.3), con la contraseña cifrada con BCrypt. Se
 * apaga con {@code sga.usuarios-iniciales.habilitados=false} (Guía 22, 4.6), no duplica un correo ya
 * registrado y falla al arrancar si una cuenta está incompleta: en producción sus datos llegan por
 * variables de entorno y un olvido no debe dejar el sistema sin administrador.
 */
@Component
@ConditionalOnProperty(name = "sga.usuarios-iniciales.habilitados", havingValue = "true", matchIfMissing = true)
public class DefaultUserInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserInitializer.class);

    private final DefaultUserProperties propiedades;
    private final UsuarioJpaDataRepository usuarios;
    private final PasswordEncoder codificador;

    public DefaultUserInitializer(DefaultUserProperties propiedades, UsuarioJpaDataRepository usuarios,
                                  PasswordEncoder codificador) {
        this.propiedades = propiedades;
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int creados = 0;
        for (DefaultUserProperties.DefaultUser inicial : propiedades.usuarios()) {
            boolean incompleta = inicial.fechaNacimiento() == null || inicial.rol() == null
                    || Stream.of(inicial.correo(), inicial.contrasena(), inicial.telefono(), inicial.documento())
                    .anyMatch(valor -> valor == null || valor.isBlank());
            if (incompleta) {
                throw new IllegalStateException("Complete los datos de la cuenta inicial del SGA.");
            }
            if (usuarios.existsByCorreoIgnoreCase(inicial.correo())) {
                continue;
            }
            usuarios.save(new UsuarioEntity(
                    inicial.nombre(),
                    inicial.correo(),
                    codificador.encode(inicial.contrasena()),
                    inicial.telefono(),
                    inicial.documento(),
                    inicial.fechaNacimiento(),
                    inicial.rol()));
            creados++;
        }
        LOG.info("Usuarios iniciales creados: {}", creados);
    }
}

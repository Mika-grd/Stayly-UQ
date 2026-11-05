package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Carga un usuario por rol al iniciar (Guía 12, 12.3), con la contraseña cifrada con BCrypt. */
@Component
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
        if (usuarios.count() > 0) {
            return;
        }
        propiedades.usuarios().forEach(inicial -> usuarios.save(new UsuarioEntity(
                inicial.nombre(),
                inicial.correo(),
                codificador.encode(inicial.contrasena()),
                inicial.telefono(),
                inicial.documento(),
                inicial.fechaNacimiento(),
                inicial.rol())));
        LOG.info("Usuarios iniciales cargados en H2: {}", usuarios.count());
    }
}

package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.request.RegistroHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.exception.ReglaAccesoException;
import co.edu.uniquindio.sga.application.security.RegistroHuespedService;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro público (actividad 7.5.2): siempre con rol HUESPED y la contraseña cifrada con BCrypt.
 * Un correo o un documento ya registrado responde 409.
 */
@Service
public class RegistroHuespedServiceImpl implements RegistroHuespedService {

    private final UsuarioJpaDataRepository usuarios;
    private final PasswordEncoder codificador;

    public RegistroHuespedServiceImpl(UsuarioJpaDataRepository usuarios, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroHuespedRequest request) {
        String correo = request.correo().trim().toLowerCase();
        String documento = request.documentoIdentidad().trim();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaAccesoException("CORREO_YA_REGISTRADO", "Ya existe un usuario con ese correo.");
        }
        if (usuarios.existsByDocumento(documento)) {
            throw new ReglaAccesoException("DOCUMENTO_YA_REGISTRADO", "Ya existe un usuario con ese documento.");
        }
        UsuarioEntity usuario = usuarios.save(new UsuarioEntity(request.nombre().trim(), correo,
                codificador.encode(request.contrasena()), request.telefono().trim(), documento,
                request.fechaNacimiento(), RolUsuario.HUESPED));
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getTelefono(),
                usuario.getRol().name(), usuario.isActivo());
    }
}

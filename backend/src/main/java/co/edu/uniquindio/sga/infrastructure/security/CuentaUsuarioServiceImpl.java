package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.request.CambioContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.ConfirmarRecuperacionRequest;
import co.edu.uniquindio.sga.application.dto.request.PerfilRequest;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReglaAccesoException;
import co.edu.uniquindio.sga.application.security.CuentaUsuarioService;
import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CodigoRecuperacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CodigoRecuperacionJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CU-39 y CU-40 (7.2): el usuario cambia su perfil (nombre, teléfono y foto; nunca el correo), cambia su
 * contraseña con la actual y la recupera con un código de 6 dígitos enviado a su correo, vigente 15
 * minutos y de un solo uso.
 */
@Service
@Transactional
public class CuentaUsuarioServiceImpl implements CuentaUsuarioService {

    static final Duration VIGENCIA_CODIGO = Duration.ofMinutes(15);
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioJpaDataRepository usuarios;
    private final CodigoRecuperacionJpaRepository codigos;
    private final PasswordEncoder codificador;
    private final NotificadorCorreo correo;
    private final Clock reloj;

    public CuentaUsuarioServiceImpl(UsuarioJpaDataRepository usuarios, CodigoRecuperacionJpaRepository codigos,
                                    PasswordEncoder codificador, NotificadorCorreo correo, Clock reloj) {
        this.usuarios = usuarios;
        this.codigos = codigos;
        this.codificador = codificador;
        this.correo = correo;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse consultarPerfil(String correoUsuario) {
        return UsuarioRespuestas.de(buscar(correoUsuario));
    }

    @Override
    public UsuarioResponse actualizarPerfil(String correoUsuario, PerfilRequest request) {
        UsuarioEntity usuario = buscar(correoUsuario);
        usuario.actualizarPerfil(request.nombre().trim(), request.telefono().trim(),
                request.fotoUrl() == null || request.fotoUrl().isBlank() ? null : request.fotoUrl().trim());
        return UsuarioRespuestas.de(usuario);
    }

    @Override
    public void cambiarContrasena(String correoUsuario, CambioContrasenaRequest request) {
        UsuarioEntity usuario = buscar(correoUsuario);
        if (!codificador.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new ReglaAccesoException("CONTRASENA_ACTUAL_INCORRECTA", "La contraseña actual no es correcta.");
        }
        usuario.cambiarContrasena(codificador.encode(request.contrasenaNueva()));
    }

    @Override
    public void solicitarRecuperacion(String correoUsuario) {
        Optional<UsuarioEntity> usuario = usuarios.findByCorreoIgnoreCase(correoUsuario.trim())
                .filter(UsuarioEntity::isActivo);
        if (usuario.isEmpty()) {
            return;   // misma respuesta exista o no el correo: no se revela quién está registrado
        }
        codigos.findByUsuarioIdAndUsadoFalse(usuario.get().getId()).forEach(CodigoRecuperacionEntity::marcarUsado);
        String codigo = "%06d".formatted(ALEATORIO.nextInt(1_000_000));
        codigos.save(new CodigoRecuperacionEntity(usuario.get().getId(), codificador.encode(codigo),
                LocalDateTime.now(reloj).plus(VIGENCIA_CODIGO)));
        correo.enviar(usuario.get().getCorreo(), "Recuperación de contraseña · Stayly-UQ",
                "Su código es " + codigo + ". Vence en 15 minutos y sirve una sola vez.");
    }

    @Override
    public void confirmarRecuperacion(ConfirmarRecuperacionRequest request) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        UsuarioEntity usuario = usuarios.findByCorreoIgnoreCase(request.correo().trim())
                .filter(UsuarioEntity::isActivo)
                .orElseThrow(RecuperacionInvalida::new);
        CodigoRecuperacionEntity vigente = codigos.findByUsuarioIdAndUsadoFalse(usuario.getId()).stream()
                .filter(codigo -> codigo.estaVigente(ahora))
                .filter(codigo -> codificador.matches(request.codigo(), codigo.getCodigoCifrado()))
                .findFirst()
                .orElseThrow(RecuperacionInvalida::new);
        vigente.marcarUsado();
        usuario.cambiarContrasena(codificador.encode(request.contrasenaNueva()));
    }

    private UsuarioEntity buscar(String correoUsuario) {
        return usuarios.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("USUARIO_NO_ENCONTRADO",
                        "No existe el usuario " + correoUsuario + "."));
    }

    /** Código inexistente, vencido, ya usado o de otro correo: siempre el mismo error. */
    private static final class RecuperacionInvalida extends ReglaAccesoException {
        RecuperacionInvalida() {
            super("CODIGO_RECUPERACION_INVALIDO", "El código de recuperación no es válido o ya venció.");
        }
    }
}

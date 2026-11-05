package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.security.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Inicio de sesión con Spring Security (Guía 12, 6.2). Vive en infraestructura porque depende del
 * {@link AuthenticationManager}.
 */
@Service
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final Duration vigencia;

    public SecurityServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                               @Value("${sga.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.vigencia = Duration.ofMinutes(expiracionMinutos);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        // 1. Busca el usuario (UserConfig) y compara la contraseña con BCrypt. Si falla lanza una
        //    AuthenticationException, que el ManejadorErroresGlobal convierte en 401.
        Authentication autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.correo(), request.contrasena()));

        // 2. Credenciales correctas: se leen el rol y el documento del usuario autenticado
        CustomUserDetails usuario = (CustomUserDetails) autenticacion.getPrincipal();
        List<String> roles = autenticacion.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 3. Se calcula la vigencia y se firma el token
        Instant ahora = Instant.now();
        String token = jwtTokenProvider.generateTokenAsString(usuario.getUsername(),
                usuario.usuario().getDocumento(), roles, ahora, ahora.plus(vigencia));

        return new TokenResponse(token, "Bearer", vigencia.toSeconds(), usuario.usuario().getRol().name());
    }
}

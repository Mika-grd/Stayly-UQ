package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Autentica /api/externo/** con la credencial del canal en el encabezado X-API-Key, en vez de un JWT
 * (contrato, esquema apiKey). Sin credencial o con una inválida responde 401 CREDENCIALES_INVALIDAS.
 * No es un {@code @Component}: lo agrega SecurityConfig a su cadena, para que no corra dos veces.
 */
public class FiltroCredencialCanal extends OncePerRequestFilter {

    public static final String ENCABEZADO = "X-API-Key";
    public static final String RUTAS = "/api/externo/";

    private final CredencialesCanalServiceImpl credenciales;
    private final RespuestasErrorSeguridad respuestasError;

    public FiltroCredencialCanal(CredencialesCanalServiceImpl credenciales, RespuestasErrorSeguridad respuestasError) {
        this.credenciales = credenciales;
        this.respuestasError = respuestasError;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return !peticion.getRequestURI().startsWith(RUTAS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        Optional<CanalExternoAutenticado> canal = credenciales.autenticar(peticion.getHeader(ENCABEZADO))
                .map(CanalExternoAutenticado::new);
        if (canal.isEmpty()) {
            SecurityContextHolder.clearContext();
            respuestasError.commence(peticion, respuesta, new BadCredentialsException("Credencial del canal inválida"));
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(canal.get());
        cadena.doFilter(peticion, respuesta);
    }
}

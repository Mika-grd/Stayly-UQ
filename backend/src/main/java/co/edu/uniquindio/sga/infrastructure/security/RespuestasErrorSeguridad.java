package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.response.ErrorNegocioResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Desafío 7.5.6: los 401 y 403 que produce la cadena de filtros (sin token, token alterado o vencido)
 * responden con el mismo {@link ErrorNegocioResponse} que el resto de la API, no con un cuerpo vacío.
 */
@Component
public class RespuestasErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final ObjectMapper json;
    private final Clock reloj;

    public RespuestasErrorSeguridad(ObjectMapper json, Clock reloj) {
        this.json = json;
        this.reloj = reloj;
    }

    /** 401: no sé quién eres. */
    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException e)
            throws IOException {
        respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escribir(respuesta, peticion, HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                "Debe enviar un token válido y vigente en el encabezado Authorization: Bearer.");
    }

    /** 403: sé quién eres, pero no tienes permiso. */
    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException e)
            throws IOException {
        escribir(respuesta, peticion, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permiso para realizar esta operación.");
    }

    private void escribir(HttpServletResponse respuesta, HttpServletRequest peticion, HttpStatus estado,
                          String codigo, String mensaje) throws IOException {
        ErrorNegocioResponse cuerpo = new ErrorNegocioResponse(codigo, mensaje, "7.2", peticion.getRequestURI(),
                OffsetDateTime.now(reloj.withZone(BOGOTA)));
        respuesta.setStatus(estado.value());
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}

package co.edu.uniquindio.sga.infrastructure.web.error;

import co.edu.uniquindio.sga.application.dto.response.ErrorNegocioResponse;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReglaAccesoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Traduce toda excepción a un {@link ErrorNegocioResponse} con su HTTP. Las reglas del dominio toman
 * su HTTP del {@link CatalogoErroresNegocio}; los recursos inexistentes son 404 y la forma del mensaje
 * (Bean Validation, JSON, parámetros) es 400. Nunca devuelve una traza técnica al cliente.
 * Seguridad (Guía 12): 401 no sé quién eres · 403 sé quién eres, pero no tienes permiso.
 */
@RestControllerAdvice
public class ManejadorErroresGlobal {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErroresGlobal.class);
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String FORMATO_INVALIDO = "FORMATO_INVALIDO";

    private final Clock reloj;

    public ManejadorErroresGlobal(Clock reloj) {
        this.reloj = reloj;
    }

    @ExceptionHandler(ReglaDominioException.class)
    ResponseEntity<ErrorNegocioResponse> reglaDominio(ReglaDominioException e, HttpServletRequest peticion) {
        CatalogoErroresNegocio.Clasificacion clasificacion = CatalogoErroresNegocio.clasificar(e.getCodigo());
        return responder(clasificacion.estado(), e.getCodigo(), e.getMessage(), clasificacion.regla(), peticion);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorNegocioResponse> recursoNoEncontrado(RecursoNoEncontradoException e,
                                                             HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, e.getCodigo(), e.getMessage(), null, peticion);
    }

    /** Seguridad (Guía 12): una regla de acceso, p. ej. CORREO_YA_REGISTRADO (409). */
    @ExceptionHandler(ReglaAccesoException.class)
    ResponseEntity<ErrorNegocioResponse> reglaAcceso(ReglaAccesoException e, HttpServletRequest peticion) {
        CatalogoErroresNegocio.Clasificacion clasificacion = CatalogoErroresNegocio.clasificar(e.getCodigo());
        return responder(clasificacion.estado(), e.getCodigo(), e.getMessage(), clasificacion.regla(), peticion);
    }

    /**
     * Seguridad (Guía 12): credenciales inválidas o usuario inactivo en el login. El mensaje es
     * genérico: no revela si el correo existe o si la contraseña era la errada.
     */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorNegocioResponse> autenticacion(AuthenticationException e, HttpServletRequest peticion) {
        return responder(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Credenciales inválidas.", "7.2",
                peticion);
    }

    /** Seguridad (Guía 12): autenticado, pero sin permiso por rol o por propietario del recurso. */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorNegocioResponse> accesoDenegado(AccessDeniedException e, HttpServletRequest peticion) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tiene permiso para realizar esta operación.",
                "7.2", peticion);
    }

    /** Un DTO anotado con @Valid no cumple sus restricciones. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorNegocioResponse> dtoInvalido(MethodArgumentNotValidException e, HttpServletRequest peticion) {
        FieldError campo = e.getBindingResult().getFieldError();
        String mensaje = campo == null
                ? "La petición no cumple el formato esperado."
                : "El campo " + campo.getField() + " " + campo.getDefaultMessage() + ".";
        return responder(HttpStatus.BAD_REQUEST, FORMATO_INVALIDO, mensaje, null, peticion);
    }

    /** Un parámetro de ruta o de consulta anotado con restricciones no las cumple. */
    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorNegocioResponse> parametroInvalido(Exception e, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, FORMATO_INVALIDO,
                "Algún parámetro de la petición no cumple el formato esperado.", null, peticion);
    }

    /** El cuerpo no es JSON válido o un valor no se puede convertir (fecha, número, enum). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorNegocioResponse> cuerpoIlegible(HttpMessageNotReadableException e,
                                                        HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, FORMATO_INVALIDO,
                "El cuerpo de la petición no tiene un formato válido.", null, peticion);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorNegocioResponse> tipoIncorrecto(MethodArgumentTypeMismatchException e,
                                                        HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, FORMATO_INVALIDO,
                "El parámetro " + e.getName() + " tiene un formato inválido.", null, peticion);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorNegocioResponse> parametroFaltante(MissingServletRequestParameterException e,
                                                           HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, FORMATO_INVALIDO,
                "El parámetro " + e.getParameterName() + " es obligatorio.", null, peticion);
    }

    /** La ruta no corresponde a ningún endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorNegocioResponse> rutaInexistente(NoResourceFoundException e, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO",
                "No existe el recurso " + peticion.getRequestURI() + ".", null, peticion);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorNegocioResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest peticion) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El método " + e.getMethod() + " no está permitido en " + peticion.getRequestURI() + ".",
                null, peticion);
    }

    /** Cualquier otro fallo: la traza queda solo en el log del servidor. */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorNegocioResponse> errorInesperado(Exception e, HttpServletRequest peticion) {
        LOG.error("Error inesperado en {} {}", peticion.getMethod(), peticion.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intente de nuevo más tarde.", null, peticion);
    }

    private ResponseEntity<ErrorNegocioResponse> responder(HttpStatus estado, String codigo, String mensaje,
                                                           String regla, HttpServletRequest peticion) {
        ErrorNegocioResponse cuerpo = new ErrorNegocioResponse(codigo, mensaje, regla, peticion.getRequestURI(),
                OffsetDateTime.now(reloj.withZone(BOGOTA)));
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

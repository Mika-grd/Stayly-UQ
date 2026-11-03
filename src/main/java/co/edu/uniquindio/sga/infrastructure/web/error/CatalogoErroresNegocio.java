package co.edu.uniquindio.sga.infrastructure.web.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Hoja "Errores de Negocio" del libro del modelo: a cada código estable le asigna su HTTP y la regla
 * que lo origina. Vive en infraestructura porque el dominio lanza el código sin conocer HTTP.
 * Criterio: 400 formato, 404 no encontrado, 422 regla sobre los datos, 409 conflicto con el estado actual.
 */
public final class CatalogoErroresNegocio {

    /** HTTP y regla de un código. {@code regla} es {@code null} cuando el error no viene de una regla. */
    public record Clasificacion(HttpStatus estado, String regla) {
    }

    private static final HttpStatus CONFLICTO = HttpStatus.CONFLICT;
    private static final HttpStatus REGLA_SOBRE_DATOS = HttpStatus.UNPROCESSABLE_ENTITY;

    private static final Map<String, Clasificacion> CATALOGO = Map.ofEntries(
            // Estancia y disponibilidad (7.5)
            Map.entry("ESTANCIA_SIN_NOCHES", new Clasificacion(REGLA_SOBRE_DATOS, "RN-03")),
            Map.entry("FECHA_ENTRADA_PASADA", new Clasificacion(REGLA_SOBRE_DATOS, "RN-04")),
            Map.entry("APARTAMENTO_INACTIVO", new Clasificacion(CONFLICTO, "7.5")),
            Map.entry("TARIFAS_INCOMPLETAS", new Clasificacion(CONFLICTO, "7.4")),
            Map.entry("CAPACIDAD_EXCEDIDA", new Clasificacion(REGLA_SOBRE_DATOS, "RN-02")),
            Map.entry("NOCHES_NO_DISPONIBLES", new Clasificacion(CONFLICTO, "RN-01")),
            Map.entry("APARTAMENTO_BLOQUEADO", new Clasificacion(CONFLICTO, "RN-07")),
            Map.entry("TIEMPO_PREPARACION_INSUFICIENTE", new Clasificacion(CONFLICTO, "RN-20")),
            Map.entry("TITULAR_NO_FACTURABLE", new Clasificacion(REGLA_SOBRE_DATOS, "3.2")),
            // Reglas propias
            Map.entry("ESTANCIA_MINIMA_NO_CUMPLIDA", new Clasificacion(REGLA_SOBRE_DATOS, "RP-01")),
            Map.entry("MASCOTAS_NO_ADMITIDAS", new Clasificacion(REGLA_SOBRE_DATOS, "RP-02")),
            Map.entry("CUPO_MASCOTAS_EXCEDIDO", new Clasificacion(REGLA_SOBRE_DATOS, "RP-02")),
            // Ciclo de vida de la reserva (sección 8)
            Map.entry("TRANSICION_INVALIDA", new Clasificacion(CONFLICTO, "RN-08")),
            Map.entry("RESERVA_NO_MODIFICABLE", new Clasificacion(CONFLICTO, "RN-14")),
            Map.entry("HORA_LLEGADA_REQUERIDA", new Clasificacion(REGLA_SOBRE_DATOS, "RN-09")),
            Map.entry("ANTICIPO_INSUFICIENTE", new Clasificacion(REGLA_SOBRE_DATOS, "L-11")),
            Map.entry("REGISTRO_ANTES_DE_ENTRADA", new Clasificacion(CONFLICTO, "RN-10")),
            Map.entry("APARTAMENTO_NO_PREPARADO", new Clasificacion(CONFLICTO, "RN-11")),
            Map.entry("NO_SHOW_ANTES_DE_HORA_LIMITE", new Clasificacion(CONFLICTO, "L-15")),
            // Folio (7.7)
            Map.entry("FOLIO_ABIERTO", new Clasificacion(CONFLICTO, "7.6")),
            Map.entry("FOLIO_CERRADO", new Clasificacion(CONFLICTO, "7.7")),
            Map.entry("FOLIO_CON_SALDO_SIN_AUTORIZACION", new Clasificacion(CONFLICTO, "RN-17")),
            Map.entry("MEDIO_PAGO_NO_ACEPTADO", new Clasificacion(REGLA_SOBRE_DATOS, "L-18")),
            Map.entry("VALOR_INVALIDO", new Clasificacion(REGLA_SOBRE_DATOS, "3.4")),
            // Apartamento, bloqueos y estado operativo (7.3, 7.6)
            Map.entry("TRANSICION_ESTADO_OPERATIVO_INVALIDA", new Clasificacion(CONFLICTO, "7.6")),
            Map.entry("APARTAMENTO_OCUPADO", new Clasificacion(CONFLICTO, "7.6")),
            Map.entry("BLOQUEO_SOBRE_RESERVAS_ACTIVAS", new Clasificacion(CONFLICTO, "7.3")),
            Map.entry("APARTAMENTO_CON_RESERVAS_ACTIVAS", new Clasificacion(CONFLICTO, "7.3")),
            Map.entry("IMAGENES_INVALIDAS", new Clasificacion(REGLA_SOBRE_DATOS, "7.3")),
            Map.entry("IDENTIFICACION_DUPLICADA", new Clasificacion(CONFLICTO, "L-04")),
            // Temporadas y política (7.4)
            Map.entry("TEMPORADAS_SOLAPADAS", new Clasificacion(CONFLICTO, "7.4")),
            Map.entry("TEMPORADA_BASE_DUPLICADA", new Clasificacion(CONFLICTO, "F-05")),
            Map.entry("TRAMOS_INSUFICIENTES", new Clasificacion(REGLA_SOBRE_DATOS, "L-10")),
            // Canal externo (7.8)
            Map.entry("CONFLICTO_CANAL", new Clasificacion(CONFLICTO, "RN-18")),
            Map.entry("CONFLICTO_YA_RESUELTO", new Clasificacion(CONFLICTO, "7.1")),
            // Usuarios y seguridad (7.2): 401 y 403 quedan listos para cuando se implemente JWT
            Map.entry("USUARIO_CON_RESERVAS_ACTIVAS", new Clasificacion(CONFLICTO, "7.2")),
            Map.entry("CORREO_YA_REGISTRADO", new Clasificacion(CONFLICTO, "7.2")),
            Map.entry("CREDENCIALES_INVALIDAS", new Clasificacion(HttpStatus.UNAUTHORIZED, "7.2")),
            Map.entry("ACCESO_DENEGADO", new Clasificacion(HttpStatus.FORBIDDEN, "7.2")),
            // Configuración y formato
            Map.entry("PARAMETRO_INVALIDO", new Clasificacion(REGLA_SOBRE_DATOS, "Sección 6")),
            Map.entry("RANGO_FECHAS_INVALIDO", new Clasificacion(HttpStatus.BAD_REQUEST, "3.1")),
            Map.entry("FORMATO_INVALIDO", new Clasificacion(HttpStatus.BAD_REQUEST, null)));

    private CatalogoErroresNegocio() {
    }

    /**
     * Clasifica un código. Los *_NO_ENCONTRADO / *_NO_ENCONTRADA son 404; un código que no está en la
     * hoja se trata como conflicto con el estado actual (409), sin regla.
     */
    public static Clasificacion clasificar(String codigo) {
        Clasificacion clasificacion = CATALOGO.get(codigo);
        if (clasificacion != null) {
            return clasificacion;
        }
        if (codigo != null && (codigo.endsWith("_NO_ENCONTRADO") || codigo.endsWith("_NO_ENCONTRADA"))) {
            return new Clasificacion(HttpStatus.NOT_FOUND, null);
        }
        return new Clasificacion(CONFLICTO, null);
    }

    /** Indica si el código está registrado en la hoja Errores de Negocio. */
    public static boolean contiene(String codigo) {
        return CATALOGO.containsKey(codigo);
    }
}

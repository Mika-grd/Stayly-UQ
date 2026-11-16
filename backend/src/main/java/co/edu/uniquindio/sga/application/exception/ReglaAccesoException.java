package co.edu.uniquindio.sga.application.exception;

/**
 * Excepción de la capa de aplicación: se violó una regla de acceso (7.2), no una regla del negocio.
 * Por ejemplo, registrar un correo que ya existe (CORREO_YA_REGISTRADO). El HTTP sale del código, igual
 * que con {@code ReglaDominioException}, a través del catálogo de errores de infraestructura.
 */
public class ReglaAccesoException extends RuntimeException {

    private final String codigo;

    public ReglaAccesoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

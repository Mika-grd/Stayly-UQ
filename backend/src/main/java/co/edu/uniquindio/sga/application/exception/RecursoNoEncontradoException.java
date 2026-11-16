package co.edu.uniquindio.sga.application.exception;

/**
 * Excepción de la capa de aplicación: el recurso pedido no existe. No es una regla del negocio
 * violada sino una búsqueda sin resultado. El {@code codigo} sigue el patrón *_NO_ENCONTRADO /
 * *_NO_ENCONTRADA de la hoja "Errores de Negocio" (p. ej. RESERVA_NO_ENCONTRADA).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private final String codigo;

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

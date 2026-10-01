package co.edu.uniquindio.sga.domain.exception;

/**
 * Única excepción del dominio. El {@code codigo} es exactamente uno de la hoja
 * "Errores de Negocio" del libro del modelo (p. ej. NOCHES_NO_DISPONIBLES).
 */
public class ReglaDominioException extends RuntimeException {

    private final String codigo;

    public ReglaDominioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

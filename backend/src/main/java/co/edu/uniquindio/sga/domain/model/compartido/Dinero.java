package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pesos colombianos (3.4). Tipo exacto sobre BigDecimal; admite negativos para los
 * movimientos inversos. Los cálculos intermedios conservan decimales y solo se
 * redondea (HALF_UP, al peso) al final de cada cargo con {@link #redondear()}.
 */
public record Dinero(BigDecimal valor) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public Dinero {
        if (valor == null) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        // Forma canónica: dos valores iguales (352000 y 352000.00) son el mismo Dinero.
        BigDecimal normalizado = valor.stripTrailingZeros();
        valor = normalizado.scale() < 0 ? normalizado.setScale(0) : normalizado;
    }

    public static Dinero pesos(long valor) {
        return new Dinero(BigDecimal.valueOf(valor));
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(valor.add(otro.valor));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(valor.subtract(otro.valor));
    }

    public Dinero multiplicar(int factor) {
        return new Dinero(valor.multiply(BigDecimal.valueOf(factor)));
    }

    /** Porcentaje exacto, sin redondear: el redondeo es responsabilidad de quien cierra el cargo. */
    public Dinero porcentaje(int porcentaje) {
        return new Dinero(valor.multiply(BigDecimal.valueOf(porcentaje)).divide(CIEN));
    }

    public Dinero negar() {
        return new Dinero(valor.negate());
    }

    public Dinero redondear() {
        return new Dinero(valor.setScale(0, RoundingMode.HALF_UP));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean esPositivo() {
        return valor.signum() > 0;
    }

    @Override
    public String toString() {
        return "$" + valor.toPlainString();
    }
}

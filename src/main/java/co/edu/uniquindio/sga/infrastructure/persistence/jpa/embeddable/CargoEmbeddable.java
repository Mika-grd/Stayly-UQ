package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Objeto de valor Cargo del folio. Nunca se actualiza: una corrección es otro cargo inverso (RN-16). */
@Embeddable
public class CargoEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCargo tipo;

    @Column(nullable = false, length = 300)
    private String concepto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected CargoEmbeddable() {
    }

    public CargoEmbeddable(TipoCargo tipo, String concepto, BigDecimal valor, LocalDateTime fecha) {
        this.tipo = tipo;
        this.concepto = concepto;
        this.valor = valor;
        this.fecha = fecha;
    }

    public TipoCargo getTipo() { return tipo; }
    public String getConcepto() { return concepto; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }
}

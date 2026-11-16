package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Objeto de valor Pago: todo pago tiene medio y fecha (RN-15). */
@Embeddable
public class PagoEmbeddable {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedioPago medio;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 150)
    private String referencia;

    protected PagoEmbeddable() {
    }

    public PagoEmbeddable(BigDecimal valor, MedioPago medio, LocalDateTime fecha, String referencia) {
        this.valor = valor;
        this.medio = medio;
        this.fecha = fecha;
        this.referencia = referencia;
    }

    public BigDecimal getValor() { return valor; }
    public MedioPago getMedio() { return medio; }
    public LocalDateTime getFecha() { return fecha; }
    public String getReferencia() { return referencia; }
}

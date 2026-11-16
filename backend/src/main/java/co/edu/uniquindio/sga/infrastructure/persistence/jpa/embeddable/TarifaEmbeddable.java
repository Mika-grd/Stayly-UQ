package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Objeto de valor Tarifa: valor por ocupante facturable por noche en una temporada, desde una fecha. */
@Embeddable
public class TarifaEmbeddable {

    @Column(name = "temporada_id", nullable = false)
    private UUID temporada;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDateTime vigenteDesde;

    protected TarifaEmbeddable() {
    }

    public TarifaEmbeddable(UUID temporada, BigDecimal valor, LocalDateTime vigenteDesde) {
        this.temporada = temporada;
        this.valor = valor;
        this.vigenteDesde = vigenteDesde;
    }

    public UUID getTemporada() { return temporada; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getVigenteDesde() { return vigenteDesde; }
}

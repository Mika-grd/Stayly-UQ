package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Objeto de valor RangoFechas [inicio, fin) guardado como dos columnas. */
@Embeddable
public class RangoFechasEmbeddable {

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fin;

    protected RangoFechasEmbeddable() {
    }

    public RangoFechasEmbeddable(LocalDate inicio, LocalDate fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public LocalDate getInicio() { return inicio; }
    public LocalDate getFin() { return fin; }
}

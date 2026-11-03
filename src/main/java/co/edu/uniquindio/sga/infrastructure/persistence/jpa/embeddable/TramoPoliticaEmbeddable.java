package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Objeto de valor TramoPolitica: antelación mínima en días y su porcentaje de retención. */
@Embeddable
public class TramoPoliticaEmbeddable {

    @Column(name = "antelacion_minima_dias", nullable = false)
    private int antelacionMinimaDias;

    @Column(name = "porcentaje_retencion", nullable = false)
    private int porcentajeRetencion;

    protected TramoPoliticaEmbeddable() {
    }

    public TramoPoliticaEmbeddable(int antelacionMinimaDias, int porcentajeRetencion) {
        this.antelacionMinimaDias = antelacionMinimaDias;
        this.porcentajeRetencion = porcentajeRetencion;
    }

    public int getAntelacionMinimaDias() { return antelacionMinimaDias; }
    public int getPorcentajeRetencion() { return porcentajeRetencion; }
}

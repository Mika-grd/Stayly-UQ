package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Objeto de valor CargoNoche: una línea del valor congelado de la reserva (RN-05, RN-22). */
@Embeddable
public class CargoNocheEmbeddable {

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 100)
    private String temporada;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "ocupantes_facturables", nullable = false)
    private int ocupantesFacturables;

    protected CargoNocheEmbeddable() {
    }

    public CargoNocheEmbeddable(LocalDate fecha, String temporada, BigDecimal tarifa, int ocupantesFacturables) {
        this.fecha = fecha;
        this.temporada = temporada;
        this.tarifa = tarifa;
        this.ocupantesFacturables = ocupantesFacturables;
    }

    public LocalDate getFecha() { return fecha; }
    public String getTemporada() { return temporada; }
    public BigDecimal getTarifa() { return tarifa; }
    public int getOcupantesFacturables() { return ocupantesFacturables; }
}

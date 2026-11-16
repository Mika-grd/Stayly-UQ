package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Objeto de valor Estancia [entrada, salida) guardado como dos columnas (3.1). */
@Embeddable
public class EstanciaEmbeddable {

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate entrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate salida;

    protected EstanciaEmbeddable() {
    }

    public EstanciaEmbeddable(LocalDate entrada, LocalDate salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public LocalDate getEntrada() { return entrada; }
    public LocalDate getSalida() { return salida; }
}

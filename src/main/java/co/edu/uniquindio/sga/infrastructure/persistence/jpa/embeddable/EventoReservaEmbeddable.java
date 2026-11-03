package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Objeto de valor EventoReserva: una entrada de la historia de la reserva. */
@Embeddable
public class EventoReservaEmbeddable {

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(length = 150)
    private String autor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected EventoReservaEmbeddable() {
    }

    public EventoReservaEmbeddable(String descripcion, String autor, LocalDateTime fecha) {
        this.descripcion = descripcion;
        this.autor = autor;
        this.fecha = fecha;
    }

    public String getDescripcion() { return descripcion; }
    public String getAutor() { return autor; }
    public LocalDateTime getFecha() { return fecha; }
}

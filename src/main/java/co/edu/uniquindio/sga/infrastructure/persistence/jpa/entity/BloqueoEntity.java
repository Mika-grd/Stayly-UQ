package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RangoFechasEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tabla del agregado Bloqueo. Levantar un bloqueo no lo borra: queda con {@code levantado = true}. */
@Entity
@Table(name = "bloqueo", indexes = @Index(name = "ix_bloqueo_apartamento", columnList = "apartamento"))
public class BloqueoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 20)
    private String apartamento;

    @Embedded
    private RangoFechasEmbeddable rango;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean levantado;

    protected BloqueoEntity() {
    }

    public BloqueoEntity(UUID id, String apartamento, RangoFechasEmbeddable rango, String motivo, String autor,
                         LocalDateTime fechaRegistro, boolean levantado) {
        this.id = id;
        this.apartamento = apartamento;
        this.rango = rango;
        this.motivo = motivo;
        this.autor = autor;
        this.fechaRegistro = fechaRegistro;
        this.levantado = levantado;
    }

    public UUID getId() { return id; }
    public String getApartamento() { return apartamento; }
    public RangoFechasEmbeddable getRango() { return rango; }
    public String getMotivo() { return motivo; }
    public String getAutor() { return autor; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public boolean isLevantado() { return levantado; }
}

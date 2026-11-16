package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tabla del agregado Novedad: el historial de daños, faltantes o situaciones de cada apartamento. */
@Entity
@Table(name = "novedad", indexes = @Index(name = "ix_novedad_apartamento", columnList = "apartamento"))
public class NovedadEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 20)
    private String apartamento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GravedadNovedad gravedad;

    protected NovedadEntity() {
    }

    public NovedadEntity(UUID id, String apartamento, LocalDateTime fecha, String autor, String descripcion,
                         GravedadNovedad gravedad) {
        this.id = id;
        this.apartamento = apartamento;
        this.fecha = fecha;
        this.autor = autor;
        this.descripcion = descripcion;
        this.gravedad = gravedad;
    }

    public UUID getId() { return id; }
    public String getApartamento() { return apartamento; }
    public LocalDateTime getFecha() { return fecha; }
    public String getAutor() { return autor; }
    public String getDescripcion() { return descripcion; }
    public GravedadNovedad getGravedad() { return gravedad; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EventoCanalEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabla del agregado Canal, con su bitácora de eventos. La bitácora solo crece (C-1). */
@Entity
@Table(name = "canal")
public class CanalEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalOrigen tipo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "canal_evento", joinColumns = @JoinColumn(name = "canal"))
    @OrderColumn(name = "orden")
    private List<EventoCanalEmbeddable> eventos = new ArrayList<>();

    protected CanalEntity() {
    }

    public CanalEntity(UUID id, String nombre, CanalOrigen tipo, List<EventoCanalEmbeddable> eventos) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.eventos = new ArrayList<>(eventos);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public CanalOrigen getTipo() { return tipo; }
    public List<EventoCanalEmbeddable> getEventos() { return eventos; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RangoFechasEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabla del agregado Temporada, con sus periodos. La base no tiene periodos (F-05). */
@Entity
@Table(name = "temporada")
public class TemporadaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "temporada_periodo", joinColumns = @JoinColumn(name = "temporada"))
    @OrderColumn(name = "orden")
    private List<RangoFechasEmbeddable> periodos = new ArrayList<>();

    @Column(name = "es_base", nullable = false)
    private boolean esBase;

    @Column(name = "estancia_minima", nullable = false)
    private int estanciaMinima;

    protected TemporadaEntity() {
    }

    public TemporadaEntity(UUID id, String nombre, List<RangoFechasEmbeddable> periodos, boolean esBase,
                           int estanciaMinima) {
        this.id = id;
        this.nombre = nombre;
        this.periodos = new ArrayList<>(periodos);
        this.esBase = esBase;
        this.estanciaMinima = estanciaMinima;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public List<RangoFechasEmbeddable> getPeriodos() { return periodos; }
    public boolean isEsBase() { return esBase; }
    public int getEstanciaMinima() { return estanciaMinima; }
}

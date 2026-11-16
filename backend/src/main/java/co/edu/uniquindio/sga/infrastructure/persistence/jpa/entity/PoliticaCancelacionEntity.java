package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TramoPoliticaEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tabla del agregado PoliticaCancelacion: una fila por versión, nunca se reemplaza una anterior (F-07). */
@Entity
@Table(name = "politica_cancelacion")
public class PoliticaCancelacionEntity {

    @Id
    @Column(name = "numero_version")
    private int version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "politica_tramo", joinColumns = @JoinColumn(name = "politica"))
    @OrderColumn(name = "orden")
    private List<TramoPoliticaEmbeddable> tramos = new ArrayList<>();

    @Column(name = "porcentaje_retencion_no_show", nullable = false)
    private int porcentajeRetencionNoShow;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDateTime vigenteDesde;

    protected PoliticaCancelacionEntity() {
    }

    public PoliticaCancelacionEntity(int version, List<TramoPoliticaEmbeddable> tramos, int porcentajeRetencionNoShow,
                                     LocalDateTime vigenteDesde) {
        this.version = version;
        this.tramos = new ArrayList<>(tramos);
        this.porcentajeRetencionNoShow = porcentajeRetencionNoShow;
        this.vigenteDesde = vigenteDesde;
    }

    public int getVersion() { return version; }
    public List<TramoPoliticaEmbeddable> getTramos() { return tramos; }
    public int getPorcentajeRetencionNoShow() { return porcentajeRetencionNoShow; }
    public LocalDateTime getVigenteDesde() { return vigenteDesde; }
}

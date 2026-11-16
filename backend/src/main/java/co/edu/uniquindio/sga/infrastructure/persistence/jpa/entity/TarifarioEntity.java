package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TarifaEmbeddable;
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

/**
 * Tabla del agregado Tarifario: uno por apartamento, con todas sus tarifas. Nunca se borra una
 * tarifa: el histórico se conserva completo (TA-2).
 */
@Entity
@Table(name = "tarifario")
public class TarifarioEntity {

    @Id
    @Column(length = 20)
    private String apartamento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tarifario_tarifa", joinColumns = @JoinColumn(name = "tarifario"))
    @OrderColumn(name = "orden")
    private List<TarifaEmbeddable> tarifas = new ArrayList<>();

    protected TarifarioEntity() {
    }

    public TarifarioEntity(String apartamento, List<TarifaEmbeddable> tarifas) {
        this.apartamento = apartamento;
        this.tarifas = new ArrayList<>(tarifas);
    }

    public String getApartamento() { return apartamento; }
    public List<TarifaEmbeddable> getTarifas() { return tarifas; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.folio.EstadoFolio;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.AutorizacionCierreEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PagoEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
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

/**
 * Tabla del agregado Folio. Un folio pertenece a una sola reserva (columna única, 7.7). El saldo
 * no se guarda: se calcula cada vez a partir de cargos y pagos (F-1).
 */
@Entity
@Table(name = "folio")
public class FolioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String reserva;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_cargo", joinColumns = @JoinColumn(name = "folio"))
    @OrderColumn(name = "orden")
    private List<CargoEmbeddable> cargos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_pago", joinColumns = @JoinColumn(name = "folio"))
    @OrderColumn(name = "orden")
    private List<PagoEmbeddable> pagos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFolio estado;

    @Embedded
    private AutorizacionCierreEmbeddable autorizacion;

    protected FolioEntity() {
    }

    public FolioEntity(UUID id, String reserva, List<CargoEmbeddable> cargos, List<PagoEmbeddable> pagos,
                       EstadoFolio estado, AutorizacionCierreEmbeddable autorizacion) {
        this.id = id;
        this.reserva = reserva;
        this.cargos = new ArrayList<>(cargos);
        this.pagos = new ArrayList<>(pagos);
        this.estado = estado;
        this.autorizacion = autorizacion;
    }

    public UUID getId() { return id; }
    public String getReserva() { return reserva; }
    public List<CargoEmbeddable> getCargos() { return cargos; }
    public List<PagoEmbeddable> getPagos() { return pagos; }
    public EstadoFolio getEstado() { return estado; }
    public AutorizacionCierreEmbeddable getAutorizacion() { return autorizacion; }
}

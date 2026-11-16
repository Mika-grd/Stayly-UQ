package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tabla del agregado ConflictoCanal: reservas externas rechazadas por chocar con una reserva
 * vigente (RN-18). La reserva vigente y el apartamento se guardan solo por su identificador.
 */
@Entity
@Table(name = "conflicto_canal")
public class ConflictoCanalEntity {

    @Id
    private UUID id;

    @Embedded
    private IdentificadorExternoEmbeddable identificadorExterno;

    @Column(nullable = false, length = 20)
    private String apartamento;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Column(name = "reserva_vigente", nullable = false, length = 20)
    private String reservaVigente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConflicto estado;

    @Column(length = 1000)
    private String resolucion;

    @Column(name = "autor_resolucion", length = 150)
    private String autorResolucion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    protected ConflictoCanalEntity() {
    }

    public ConflictoCanalEntity(UUID id, IdentificadorExternoEmbeddable identificadorExterno, String apartamento,
                                EstanciaEmbeddable estancia, String reservaVigente, EstadoConflicto estado,
                                String resolucion, String autorResolucion, LocalDateTime fechaResolucion,
                                LocalDateTime fechaRegistro) {
        this.id = id;
        this.identificadorExterno = identificadorExterno;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.reservaVigente = reservaVigente;
        this.estado = estado;
        this.resolucion = resolucion;
        this.autorResolucion = autorResolucion;
        this.fechaResolucion = fechaResolucion;
        this.fechaRegistro = fechaRegistro;
    }

    public UUID getId() { return id; }
    public IdentificadorExternoEmbeddable getIdentificadorExterno() { return identificadorExterno; }
    public String getApartamento() { return apartamento; }
    public EstanciaEmbeddable getEstancia() { return estancia; }
    public String getReservaVigente() { return reservaVigente; }
    public EstadoConflicto getEstado() { return estado; }
    public String getResolucion() { return resolucion; }
    public String getAutorResolucion() { return autorResolucion; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}

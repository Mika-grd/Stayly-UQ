package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EventoReservaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla del agregado Reserva. El apartamento se guarda solo por su identificación (referencia
 * entre agregados por identificador). La restricción única sobre canal + identificador externo
 * protege RN-19 también en la base de datos.
 */
@Entity
@Table(name = "reserva",
        uniqueConstraints = @UniqueConstraint(name = "uk_reserva_externa",
                columnNames = {"externo_canal", "externo_codigo"}),
        indexes = @Index(name = "ix_reserva_apartamento_estado", columnList = "apartamento, estado"))
public class ReservaEntity {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 20)
    private String apartamento;

    @Embedded
    private EstanciaEmbeddable estancia;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "reserva", nullable = false)
    @OrderColumn(name = "orden")
    private List<OcupanteEntity> ocupantes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalOrigen canal;

    @Embedded
    private IdentificadorExternoEmbeddable identificadorExterno;

    @Column(name = "hora_estimada_llegada")
    private LocalTime horaEstimadaLlegada;

    @Column(nullable = false)
    private int mascotas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_cargo_noche", joinColumns = @JoinColumn(name = "reserva"))
    @OrderColumn(name = "orden")
    private List<CargoNocheEmbeddable> valor = new ArrayList<>();

    @Column(name = "version_politica", nullable = false)
    private int versionPolitica;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_evento", joinColumns = @JoinColumn(name = "reserva"))
    @OrderColumn(name = "orden")
    private List<EventoReservaEmbeddable> eventos = new ArrayList<>();

    protected ReservaEntity() {
    }

    public ReservaEntity(String codigo, String apartamento, EstanciaEmbeddable estancia, List<OcupanteEntity> ocupantes,
                         EstadoReserva estado, CanalOrigen canal, IdentificadorExternoEmbeddable identificadorExterno,
                         LocalTime horaEstimadaLlegada, int mascotas, List<CargoNocheEmbeddable> valor,
                         int versionPolitica, LocalDateTime fechaCreacion, List<EventoReservaEmbeddable> eventos) {
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = new ArrayList<>(ocupantes);
        this.estado = estado;
        this.canal = canal;
        this.identificadorExterno = identificadorExterno;
        this.horaEstimadaLlegada = horaEstimadaLlegada;
        this.mascotas = mascotas;
        this.valor = new ArrayList<>(valor);
        this.versionPolitica = versionPolitica;
        this.fechaCreacion = fechaCreacion;
        this.eventos = new ArrayList<>(eventos);
    }

    public String getCodigo() { return codigo; }
    public String getApartamento() { return apartamento; }
    public EstanciaEmbeddable getEstancia() { return estancia; }
    public List<OcupanteEntity> getOcupantes() { return ocupantes; }
    public EstadoReserva getEstado() { return estado; }
    public CanalOrigen getCanal() { return canal; }
    public IdentificadorExternoEmbeddable getIdentificadorExterno() { return identificadorExterno; }
    public LocalTime getHoraEstimadaLlegada() { return horaEstimadaLlegada; }
    public int getMascotas() { return mascotas; }
    public List<CargoNocheEmbeddable> getValor() { return valor; }
    public int getVersionPolitica() { return versionPolitica; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public List<EventoReservaEmbeddable> getEventos() { return eventos; }
}

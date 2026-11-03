package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.ImagenApartamentoEmbeddable;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tabla del agregado Apartamento, separada del dominio. {@code activo} es la eliminación lógica
 * (7.3): un apartamento retirado sigue en la tabla y las reservas históricas lo referencian.
 */
@Entity
@Table(name = "apartamento")
public class ApartamentoEntity {

    @Id
    @Column(length = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Column(nullable = false)
    private int dormitorios;

    @Column(nullable = false)
    private int capacidad;

    @Column(name = "admite_mascotas", nullable = false)
    private boolean admiteMascotas;

    @Column(name = "advertencia_acceso", length = 300)
    private String advertenciaAcceso;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartamento_caracteristica", joinColumns = @JoinColumn(name = "apartamento"))
    @Column(name = "nombre", nullable = false, length = 100)
    private Set<String> caracteristicas = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartamento_imagen", joinColumns = @JoinColumn(name = "apartamento"))
    @OrderColumn(name = "orden")
    private List<ImagenApartamentoEmbeddable> imagenes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 30)
    private EstadoOperativo estadoOperativo;

    @Column(nullable = false)
    private boolean activo;

    protected ApartamentoEntity() {
    }

    public ApartamentoEntity(String identificacion, String nombre, String descripcion, int dormitorios, int capacidad,
                             boolean admiteMascotas, String advertenciaAcceso, Set<String> caracteristicas,
                             List<ImagenApartamentoEmbeddable> imagenes, EstadoOperativo estadoOperativo,
                             boolean activo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.admiteMascotas = admiteMascotas;
        this.advertenciaAcceso = advertenciaAcceso;
        this.caracteristicas = new HashSet<>(caracteristicas);
        this.imagenes = new ArrayList<>(imagenes);
        this.estadoOperativo = estadoOperativo;
        this.activo = activo;
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getDormitorios() { return dormitorios; }
    public int getCapacidad() { return capacidad; }
    public boolean isAdmiteMascotas() { return admiteMascotas; }
    public String getAdvertenciaAcceso() { return advertenciaAcceso; }
    public Set<String> getCaracteristicas() { return caracteristicas; }
    public List<ImagenApartamentoEmbeddable> getImagenes() { return imagenes; }
    public EstadoOperativo getEstadoOperativo() { return estadoOperativo; }
    public boolean isActivo() { return activo; }
}

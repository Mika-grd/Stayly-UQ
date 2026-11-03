package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.DatosContactoTitularEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Tabla de la entidad interna Ocupante. Tiene identidad propia (IdOcupante), por eso es una
 * entidad y no un objeto de valor; solo se guarda a través de su raíz, la Reserva.
 */
@Entity
@Table(name = "ocupante")
public class OcupanteEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 30)
    private String documento;

    @Column(nullable = false)
    private boolean titular;

    @Embedded
    private DatosContactoTitularEmbeddable contacto;

    protected OcupanteEntity() {
    }

    public OcupanteEntity(UUID id, String nombre, LocalDate fechaNacimiento, String documento, boolean titular,
                          DatosContactoTitularEmbeddable contacto) {
        this.id = id;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.documento = documento;
        this.titular = titular;
        this.contacto = contacto;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getDocumento() { return documento; }
    public boolean isTitular() { return titular; }
    public DatosContactoTitularEmbeddable getContacto() { return contacto; }
}

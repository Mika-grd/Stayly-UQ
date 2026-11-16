package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Credencial de un canal externo (X-API-Key). Concepto de acceso: vive en infraestructura, como el
 * usuario (3.6). Solo se guarda la huella SHA-256, nunca la credencial en claro.
 */
@Entity
@Table(name = "credencial_canal")
public class CredencialCanalEntity {

    @Id
    private UUID canal;

    @Column(nullable = false, unique = true, length = 64)
    private String huella;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    protected CredencialCanalEntity() {
    }

    public CredencialCanalEntity(UUID canal, String huella, boolean activa, LocalDateTime fechaEmision) {
        this.canal = canal;
        this.huella = huella;
        this.activa = activa;
        this.fechaEmision = fechaEmision;
    }

    public UUID getCanal() { return canal; }
    public String getHuella() { return huella; }
    public boolean isActiva() { return activa; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
}

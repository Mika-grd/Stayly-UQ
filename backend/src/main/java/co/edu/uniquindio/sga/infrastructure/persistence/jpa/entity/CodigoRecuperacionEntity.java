package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Código de recuperación de contraseña (7.2): vigencia de 15 minutos y un solo uso. Se guarda su hash
 * BCrypt, nunca el código en claro.
 */
@Entity
@Table(name = "codigo_recuperacion")
public class CodigoRecuperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "codigo_cifrado", nullable = false, length = 100)
    private String codigoCifrado;

    @Column(name = "vence_en", nullable = false)
    private LocalDateTime venceEn;

    @Column(nullable = false)
    private boolean usado;

    protected CodigoRecuperacionEntity() {
    }

    public CodigoRecuperacionEntity(UUID usuarioId, String codigoCifrado, LocalDateTime venceEn) {
        this.usuarioId = usuarioId;
        this.codigoCifrado = codigoCifrado;
        this.venceEn = venceEn;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getCodigoCifrado() { return codigoCifrado; }
    public LocalDateTime getVenceEn() { return venceEn; }
    public boolean isUsado() { return usado; }

    public boolean estaVigente(LocalDateTime ahora) {
        return !usado && ahora.isBefore(venceEn);
    }

    public void marcarUsado() {
        this.usado = true;
    }
}

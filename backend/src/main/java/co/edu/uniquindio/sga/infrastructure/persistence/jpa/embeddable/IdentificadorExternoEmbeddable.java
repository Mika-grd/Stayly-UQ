package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

/** Objeto de valor IdentificadorExterno: canal + código propio del canal (RN-19). */
@Embeddable
public class IdentificadorExternoEmbeddable {

    @Column(name = "externo_canal")
    private UUID canal;

    @Column(name = "externo_codigo", length = 100)
    private String codigo;

    protected IdentificadorExternoEmbeddable() {
    }

    public IdentificadorExternoEmbeddable(UUID canal, String codigo) {
        this.canal = canal;
        this.codigo = codigo;
    }

    public UUID getCanal() { return canal; }
    public String getCodigo() { return codigo; }
}

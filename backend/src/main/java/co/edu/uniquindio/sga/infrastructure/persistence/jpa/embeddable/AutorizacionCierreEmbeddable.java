package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Objeto de valor AutorizacionCierre: quién autorizó cerrar el folio con saldo, por qué y cuándo (RN-17). */
@Embeddable
public class AutorizacionCierreEmbeddable {

    @Column(name = "autorizacion_autor", length = 150)
    private String autor;

    @Column(name = "autorizacion_motivo", length = 500)
    private String motivo;

    @Column(name = "autorizacion_fecha")
    private LocalDateTime fecha;

    protected AutorizacionCierreEmbeddable() {
    }

    public AutorizacionCierreEmbeddable(String autor, String motivo, LocalDateTime fecha) {
        this.autor = autor;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public String getAutor() { return autor; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }
}

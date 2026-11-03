package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Objeto de valor EventoCanal: una anotación de la bitácora del canal (7.8). */
@Embeddable
public class EventoCanalEmbeddable {

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "identificador_externo", length = 100)
    private String identificadorExterno;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 500)
    private String resultado;

    protected EventoCanalEmbeddable() {
    }

    public EventoCanalEmbeddable(String tipo, String identificadorExterno, LocalDateTime fecha, String resultado) {
        this.tipo = tipo;
        this.identificadorExterno = identificadorExterno;
        this.fecha = fecha;
        this.resultado = resultado;
    }

    public String getTipo() { return tipo; }
    public String getIdentificadorExterno() { return identificadorExterno; }
    public LocalDateTime getFecha() { return fecha; }
    public String getResultado() { return resultado; }
}

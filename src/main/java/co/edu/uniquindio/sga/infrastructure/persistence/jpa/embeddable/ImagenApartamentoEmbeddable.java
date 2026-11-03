package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Objeto de valor ImagenApartamento: enlace y si es la principal (7.3). */
@Embeddable
public class ImagenApartamentoEmbeddable {

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private boolean principal;

    protected ImagenApartamentoEmbeddable() {
    }

    public ImagenApartamentoEmbeddable(String url, boolean principal) {
        this.url = url;
        this.principal = principal;
    }

    public String getUrl() { return url; }
    public boolean isPrincipal() { return principal; }
}

package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Imagen del apartamento: enlace y si es la principal (7.3). */
public record ImagenApartamento(String url, boolean principal) {

    public ImagenApartamento {
        if (url == null || url.isBlank()) {
            throw new ReglaDominioException("IMAGENES_INVALIDAS", "La imagen del apartamento debe tener un enlace.");
        }
        url = url.trim();
    }
}

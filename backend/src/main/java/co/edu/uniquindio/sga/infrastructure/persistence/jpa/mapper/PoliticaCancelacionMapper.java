package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TramoPoliticaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;

/**
 * Traduce entre el agregado PoliticaCancelacion y su tabla. Se rearma con {@code crearVersion}:
 * cada versión es inmutable y sus validaciones (P-1) son solo de forma.
 */
public final class PoliticaCancelacionMapper {

    private PoliticaCancelacionMapper() {
    }

    public static PoliticaCancelacionEntity aEntidad(PoliticaCancelacion politica) {
        return new PoliticaCancelacionEntity(politica.version().numero(),
                politica.tramos().stream()
                        .map(tramo -> new TramoPoliticaEmbeddable(tramo.antelacionMinimaDias(),
                                tramo.porcentajeRetencion()))
                        .toList(),
                politica.porcentajeRetencionNoShow(), politica.vigenteDesde());
    }

    public static PoliticaCancelacion aDominio(PoliticaCancelacionEntity entidad) {
        return PoliticaCancelacion.crearVersion(new VersionPolitica(entidad.getVersion()),
                entidad.getTramos().stream()
                        .map(tramo -> new TramoPolitica(tramo.getAntelacionMinimaDias(), tramo.getPorcentajeRetencion()))
                        .toList(),
                entidad.getPorcentajeRetencionNoShow(), entidad.getVigenteDesde());
    }
}

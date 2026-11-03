package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RangoFechasEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;

/** Traduce entre el agregado Bloqueo y su tabla. Sin lógica de negocio. */
public final class BloqueoMapper {

    private BloqueoMapper() {
    }

    public static BloqueoEntity aEntidad(Bloqueo bloqueo) {
        return new BloqueoEntity(bloqueo.id().valor(), bloqueo.apartamento().valor(),
                new RangoFechasEmbeddable(bloqueo.rango().inicio(), bloqueo.rango().fin()), bloqueo.motivo(),
                bloqueo.autor(), bloqueo.fechaRegistro(), bloqueo.estaLevantado());
    }

    public static Bloqueo aDominio(BloqueoEntity entidad) {
        return Bloqueo.reconstituir(new IdBloqueo(entidad.getId()), new IdentificacionApartamento(entidad.getApartamento()),
                new RangoFechas(entidad.getRango().getInicio(), entidad.getRango().getFin()), entidad.getMotivo(),
                entidad.getAutor(), entidad.getFechaRegistro(), entidad.isLevantado());
    }
}

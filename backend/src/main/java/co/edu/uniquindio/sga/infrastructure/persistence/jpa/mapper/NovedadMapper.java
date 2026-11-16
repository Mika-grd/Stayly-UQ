package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.IdNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;

/**
 * Traduce entre el agregado Novedad y su tabla. Se rearma con {@code registrar}, pasándole la
 * fecha guardada: una novedad no cambia después de registrarse.
 */
public final class NovedadMapper {

    private NovedadMapper() {
    }

    public static NovedadEntity aEntidad(Novedad novedad) {
        return new NovedadEntity(novedad.id().valor(), novedad.apartamento().valor(), novedad.fecha(), novedad.autor(),
                novedad.descripcion(), novedad.gravedad());
    }

    public static Novedad aDominio(NovedadEntity entidad) {
        return Novedad.registrar(new IdNovedad(entidad.getId()), new IdentificacionApartamento(entidad.getApartamento()),
                entidad.getAutor(), entidad.getDescripcion(), entidad.getGravedad(), entidad.getFecha());
    }
}

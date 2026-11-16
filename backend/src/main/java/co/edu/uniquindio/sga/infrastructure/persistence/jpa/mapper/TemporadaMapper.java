package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RangoFechasEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;

/** Traduce entre el agregado Temporada y su tabla. Sin lógica de negocio. */
public final class TemporadaMapper {

    private TemporadaMapper() {
    }

    public static TemporadaEntity aEntidad(Temporada temporada) {
        return new TemporadaEntity(temporada.id().valor(), temporada.nombre(),
                temporada.periodos().stream()
                        .map(periodo -> new RangoFechasEmbeddable(periodo.inicio(), periodo.fin()))
                        .toList(),
                temporada.esBase(), temporada.estanciaMinima().noches());
    }

    public static Temporada aDominio(TemporadaEntity entidad) {
        return Temporada.reconstituir(new IdTemporada(entidad.getId()), entidad.getNombre(),
                entidad.getPeriodos().stream()
                        .map(periodo -> new RangoFechas(periodo.getInicio(), periodo.getFin()))
                        .toList(),
                entidad.isEsBase(), new EstanciaMinima(entidad.getEstanciaMinima()));
    }
}

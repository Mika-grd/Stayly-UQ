package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EventoCanalEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CanalEntity;

/**
 * Traduce entre el agregado Canal y su tabla. Se rearma con su propia API pública
 * ({@code registrar} + {@code registrarEvento}), en el orden de la bitácora.
 */
public final class CanalMapper {

    private CanalMapper() {
    }

    public static CanalEntity aEntidad(Canal canal) {
        return new CanalEntity(canal.id().valor(), canal.nombre(), canal.tipo(),
                canal.eventos().stream()
                        .map(evento -> new EventoCanalEmbeddable(evento.tipo(), evento.identificadorExterno(),
                                evento.fecha(), evento.resultado()))
                        .toList());
    }

    public static Canal aDominio(CanalEntity entidad) {
        Canal canal = Canal.registrar(new IdCanal(entidad.getId()), entidad.getNombre(), entidad.getTipo());
        entidad.getEventos().forEach(evento -> canal.registrarEvento(new EventoCanal(evento.getTipo(),
                evento.getIdentificadorExterno(), evento.getFecha(), evento.getResultado())));
        return canal;
    }
}

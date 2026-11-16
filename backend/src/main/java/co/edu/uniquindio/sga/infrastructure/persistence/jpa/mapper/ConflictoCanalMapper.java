package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConflictoCanalEntity;

/** Traduce entre el agregado ConflictoCanal y su tabla. Sin lógica de negocio. */
public final class ConflictoCanalMapper {

    private ConflictoCanalMapper() {
    }

    public static ConflictoCanalEntity aEntidad(ConflictoCanal conflicto) {
        return new ConflictoCanalEntity(conflicto.id().valor(),
                new IdentificadorExternoEmbeddable(conflicto.identificadorExterno().canal().valor(),
                        conflicto.identificadorExterno().codigo()),
                conflicto.apartamento().valor(),
                new EstanciaEmbeddable(conflicto.estancia().entrada(), conflicto.estancia().salida()),
                conflicto.reservaVigente().valor(), conflicto.estado(), conflicto.resolucion().orElse(null),
                conflicto.autorResolucion().orElse(null), conflicto.fechaResolucion().orElse(null),
                conflicto.fechaRegistro());
    }

    public static ConflictoCanal aDominio(ConflictoCanalEntity entidad) {
        return ConflictoCanal.reconstituir(new IdConflictoCanal(entidad.getId()),
                new IdentificadorExterno(new IdCanal(entidad.getIdentificadorExterno().getCanal()),
                        entidad.getIdentificadorExterno().getCodigo()),
                new IdentificacionApartamento(entidad.getApartamento()),
                new Estancia(entidad.getEstancia().getEntrada(), entidad.getEstancia().getSalida()),
                new CodigoReserva(entidad.getReservaVigente()), entidad.getFechaRegistro(), entidad.getEstado(),
                entidad.getAutorResolucion(), entidad.getResolucion(), entidad.getFechaResolucion());
    }
}

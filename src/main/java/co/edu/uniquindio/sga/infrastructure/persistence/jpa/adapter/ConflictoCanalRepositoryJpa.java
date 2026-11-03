package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ConflictoCanalMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConflictoCanalJpaRepository;

import java.util.List;

/** Adaptador JPA del puerto ConflictoCanalRepository. */
public class ConflictoCanalRepositoryJpa implements ConflictoCanalRepository {

    private final ConflictoCanalJpaRepository jpa;

    public ConflictoCanalRepositoryJpa(ConflictoCanalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(ConflictoCanal conflicto) {
        jpa.save(ConflictoCanalMapper.aEntidad(conflicto));
    }

    @Override
    public List<ConflictoCanal> listarPendientes() {
        return jpa.findByEstadoOrderByFechaRegistroAsc(EstadoConflicto.PENDIENTE).stream()
                .map(ConflictoCanalMapper::aDominio)
                .toList();
    }
}

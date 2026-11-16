package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConflictoCanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConflictoCanalJpaRepository extends JpaRepository<ConflictoCanalEntity, UUID> {

    /** Los conflictos por revisar, del más antiguo al más reciente. */
    List<ConflictoCanalEntity> findByEstadoOrderByFechaRegistroAsc(EstadoConflicto estado);
}

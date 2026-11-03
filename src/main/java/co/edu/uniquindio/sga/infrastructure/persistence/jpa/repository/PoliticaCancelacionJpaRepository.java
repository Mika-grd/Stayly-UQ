package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliticaCancelacionJpaRepository extends JpaRepository<PoliticaCancelacionEntity, Integer> {

    /** La vigente es la versión de mayor número. */
    Optional<PoliticaCancelacionEntity> findFirstByOrderByVersionDesc();
}

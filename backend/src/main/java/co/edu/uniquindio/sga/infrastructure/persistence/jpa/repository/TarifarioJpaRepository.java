package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifarioJpaRepository extends JpaRepository<TarifarioEntity, String> {
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CanalJpaRepository extends JpaRepository<CanalEntity, UUID> {
}

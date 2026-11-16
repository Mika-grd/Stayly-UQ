package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FolioJpaRepository extends JpaRepository<FolioEntity, UUID> {

    Optional<FolioEntity> findByReserva(String reserva);
}

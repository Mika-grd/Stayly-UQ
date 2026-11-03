package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NovedadJpaRepository extends JpaRepository<NovedadEntity, UUID> {

    List<NovedadEntity> findByApartamentoOrderByFechaAsc(String apartamento);
}

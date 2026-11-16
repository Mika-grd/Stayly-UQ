package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NovedadJpaRepository extends JpaRepository<NovedadEntity, UUID> {

    List<NovedadEntity> findByApartamentoOrderByFechaAsc(String apartamento);

    Page<NovedadEntity> findByApartamento(String apartamento, Pageable pagina);
}

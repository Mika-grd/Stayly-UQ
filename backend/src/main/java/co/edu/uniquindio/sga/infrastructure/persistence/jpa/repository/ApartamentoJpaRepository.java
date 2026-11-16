package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartamentoJpaRepository extends JpaRepository<ApartamentoEntity, String> {

    /** Los listados ignoran los apartamentos retirados (eliminación lógica, 7.3). */
    List<ApartamentoEntity> findByActivoTrue();
}

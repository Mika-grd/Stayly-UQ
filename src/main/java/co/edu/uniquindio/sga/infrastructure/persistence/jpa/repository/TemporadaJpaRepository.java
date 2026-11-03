package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemporadaJpaRepository extends JpaRepository<TemporadaEntity, UUID> {

    /** Temporadas con un periodo [inicio, fin) que contiene esa noche. */
    @Query("select distinct t from TemporadaEntity t join t.periodos p where p.inicio <= :noche and p.fin > :noche")
    List<TemporadaEntity> buscarQueContienen(@Param("noche") LocalDate noche);

    Optional<TemporadaEntity> findFirstByEsBaseTrue();
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolioJpaRepository extends JpaRepository<FolioEntity, UUID> {

    Optional<FolioEntity> findByReserva(String reserva);

    @Query("""
            select distinct f from FolioEntity f join f.pagos p
            where p.fecha >= :desde and p.fecha < :hasta""")
    List<FolioEntity> buscarConPagosEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}

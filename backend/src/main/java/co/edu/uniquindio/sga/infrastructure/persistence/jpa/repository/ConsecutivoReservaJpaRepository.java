package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConsecutivoReservaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConsecutivoReservaJpaRepository extends JpaRepository<ConsecutivoReservaEntity, Integer> {

    /** Bloquea la fila del año: dos reservas simultáneas nunca reciben el mismo número. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConsecutivoReservaEntity c where c.anio = :anio")
    Optional<ConsecutivoReservaEntity> bloquearAnio(@Param("anio") int anio);
}

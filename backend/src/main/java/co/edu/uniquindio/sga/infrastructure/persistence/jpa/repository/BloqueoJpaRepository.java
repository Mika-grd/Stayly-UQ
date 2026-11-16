package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BloqueoJpaRepository extends JpaRepository<BloqueoEntity, UUID> {

    /** Bloqueos sin levantar cuyo rango [inicio, fin) comparte al menos una noche con [entrada, salida) (RN-07). */
    @Query("""
            select b from BloqueoEntity b
            where b.apartamento = :apartamento and b.levantado = false
              and b.rango.inicio < :salida and :entrada < b.rango.fin""")
    List<BloqueoEntity> buscarVigentesQueSolapan(@Param("apartamento") String apartamento,
                                                 @Param("entrada") LocalDate entrada,
                                                 @Param("salida") LocalDate salida);
}

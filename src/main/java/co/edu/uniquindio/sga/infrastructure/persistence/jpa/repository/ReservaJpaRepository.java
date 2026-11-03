package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, String> {

    /** Solapamiento de 3.1: entrada_A < salida_B y entrada_B < salida_A. */
    @Query("""
            select r from ReservaEntity r
            where r.apartamento = :apartamento and r.estado in :estados
              and r.estancia.entrada < :salida and :entrada < r.estancia.salida""")
    List<ReservaEntity> buscarQueSolapan(@Param("apartamento") String apartamento,
                                         @Param("estados") Collection<EstadoReserva> estados,
                                         @Param("entrada") LocalDate entrada, @Param("salida") LocalDate salida);

    Optional<ReservaEntity> findByIdentificadorExternoCanalAndIdentificadorExternoCodigo(UUID canal, String codigo);

    List<ReservaEntity> findByEstadoAndFechaCreacionBefore(EstadoReserva estado, LocalDateTime limite);

    boolean existsByApartamentoAndEstadoInAndEstanciaSalidaAfter(String apartamento,
                                                                 Collection<EstadoReserva> estados, LocalDate hoy);
}

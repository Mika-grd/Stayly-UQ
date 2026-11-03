package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<ReservaEntity> findByApartamentoAndEstadoInAndEstanciaSalidaAfter(String apartamento,
                                                                          Collection<EstadoReserva> estados,
                                                                          LocalDate hoy);

    /**
     * Listado de 7.5 con filtros opcionales (un parámetro nulo no filtra). {@code patron} busca en el
     * nombre, el documento o el correo del titular. El orden y la página llegan en el Pageable.
     */
    @Query(value = """
            select r from ReservaEntity r
            where (:estado is null or r.estado = :estado)
              and (:canal is null or r.canal = :canal)
              and (:apartamento is null or r.apartamento = :apartamento)
              and (:desde is null or r.estancia.salida > :desde)
              and (:hasta is null or r.estancia.entrada < :hasta)
              and (:patron is null or exists (
                    select o from r.ocupantes o
                    where o.titular = true
                      and (lower(o.nombre) like :patron or lower(o.documento) like :patron
                           or lower(o.contacto.correo) like :patron)))""",
            countQuery = """
            select count(r) from ReservaEntity r
            where (:estado is null or r.estado = :estado)
              and (:canal is null or r.canal = :canal)
              and (:apartamento is null or r.apartamento = :apartamento)
              and (:desde is null or r.estancia.salida > :desde)
              and (:hasta is null or r.estancia.entrada < :hasta)
              and (:patron is null or exists (
                    select o from r.ocupantes o
                    where o.titular = true
                      and (lower(o.nombre) like :patron or lower(o.documento) like :patron
                           or lower(o.contacto.correo) like :patron)))""")
    Page<ReservaEntity> buscar(@Param("estado") EstadoReserva estado, @Param("canal") CanalOrigen canal,
                               @Param("apartamento") String apartamento, @Param("desde") LocalDate desde,
                               @Param("hasta") LocalDate hasta, @Param("patron") String patron, Pageable pagina);
}

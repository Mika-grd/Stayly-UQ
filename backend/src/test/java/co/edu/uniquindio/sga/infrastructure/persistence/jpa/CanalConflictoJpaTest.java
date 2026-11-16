package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.CanalRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ConflictoCanalRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ConflictoCanalMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CanalJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConflictoCanalJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Canal y ConflictoCanal van y vuelven de H2 sin perder la bitácora ni la resolución. */
@DataJpaTest
class CanalConflictoJpaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final IdCanal ID_CANAL = new IdCanal(UUID.randomUUID());

    @Autowired
    private TestEntityManager em;
    @Autowired
    private CanalJpaRepository canalJpa;
    @Autowired
    private ConflictoCanalJpaRepository conflictoJpa;

    private CanalRepositoryJpa canales;
    private ConflictoCanalRepositoryJpa conflictos;

    @BeforeEach
    void crearAdaptadores() {
        canales = new CanalRepositoryJpa(canalJpa);
        conflictos = new ConflictoCanalRepositoryJpa(conflictoJpa);
    }

    @Test
    @DisplayName("Canal · Se guarda y se reconstituye con su bitácora en orden (C-1, 7.8)")
    void guardarYLeerCanalConBitacora() {
        // Arrange
        Canal canal = Canal.registrar(ID_CANAL, "Plataforma de reservas", CanalOrigen.EXTERNO);
        canal.registrarEvento(new EventoCanal("RESERVA_RECIBIDA", "BK-7781", AHORA, "ACEPTADA"));
        canal.registrarEvento(new EventoCanal("RESERVA_RECIBIDA", "BK-7781", AHORA.plusMinutes(2), "YA_EXISTE"));

        // Act
        canales.guardar(canal);
        vaciarCache();
        Canal leido = canales.buscarPorId(ID_CANAL).orElseThrow();

        // Assert
        assertThat(leido).isEqualTo(canal);
        assertThat(leido.nombre()).isEqualTo("Plataforma de reservas");
        assertThat(leido.tipo()).isEqualTo(CanalOrigen.EXTERNO);
        assertThat(leido.eventos()).containsExactlyElementsOf(canal.eventos());
    }

    @Test
    @DisplayName("Conflicto · Solo los PENDIENTE aparecen para revisión y el resuelto conserva su resolución (RN-18)")
    void listarConflictosPendientes() {
        // Arrange
        ConflictoCanal antiguo = conflicto("BK-1001", AHORA);
        ConflictoCanal reciente = conflicto("BK-1002", AHORA.plusHours(2));
        ConflictoCanal resuelto = conflicto("BK-1003", AHORA.plusHours(1));
        resuelto.resolver("admin@stayly-uq.co", "Se reubicó al huésped del canal en SUQ-202", AHORA.plusHours(3));
        conflictos.guardar(reciente);
        conflictos.guardar(antiguo);
        conflictos.guardar(resuelto);
        vaciarCache();

        // Act + Assert
        assertThat(conflictos.listarPendientes()).containsExactly(antiguo, reciente);
        ConflictoCanal leidoResuelto = conflictoJpa.findById(resuelto.id().valor())
                .map(ConflictoCanalMapper::aDominio)
                .orElseThrow();
        assertThat(leidoResuelto.estado()).isEqualTo(EstadoConflicto.RESUELTO);
        assertThat(leidoResuelto.resolucion()).contains("Se reubicó al huésped del canal en SUQ-202");
        assertThat(leidoResuelto.identificadorExterno()).isEqualTo(resuelto.identificadorExterno());
        assertThat(leidoResuelto.estancia()).isEqualTo(resuelto.estancia());
    }

    private static ConflictoCanal conflicto(String codigoExterno, LocalDateTime registro) {
        return ConflictoCanal.registrar(new IdConflictoCanal(UUID.randomUUID()),
                new IdentificadorExterno(ID_CANAL, codigoExterno), new IdentificacionApartamento("SUQ-201"),
                new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 15)),
                new CodigoReserva("RES-2026-00003"), registro);
    }

    /** Obliga a leer de H2 y no de la caché de primer nivel de JPA. */
    private void vaciarCache() {
        em.flush();
        em.clear();
    }
}

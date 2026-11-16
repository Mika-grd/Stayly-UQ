package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ApartamentoRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.TarifarioRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.TemporadaRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TarifarioJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TemporadaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.ID_ALTA;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.ID_BASE;
import static org.assertj.core.api.Assertions.assertThat;

/** Cada agregado va y vuelve de H2 sin perder estado: lo guardado es lo que se reconstituye. */
@DataJpaTest
class ApartamentoTemporadaTarifarioJpaTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ApartamentoJpaRepository apartamentoJpa;
    @Autowired
    private TemporadaJpaRepository temporadaJpa;
    @Autowired
    private TarifarioJpaRepository tarifarioJpa;

    private ApartamentoRepositoryJpa apartamentos;
    private TemporadaRepositoryJpa temporadas;
    private TarifarioRepositoryJpa tarifarios;

    @BeforeEach
    void crearAdaptadores() {
        apartamentos = new ApartamentoRepositoryJpa(apartamentoJpa);
        temporadas = new TemporadaRepositoryJpa(temporadaJpa);
        tarifarios = new TarifarioRepositoryJpa(tarifarioJpa);
    }

    @Test
    @DisplayName("Apartamento · Se guarda y se reconstituye con su estado operativo, imágenes y advertencia")
    void guardarYLeerApartamento() {
        // Arrange
        Apartamento suq301 = FichaStaylyUq.suq301();

        // Act
        apartamentos.guardar(suq301);
        vaciarCache();
        Apartamento leido = apartamentos.buscarPorIdentificacion(suq301.identificacion()).orElseThrow();

        // Assert
        assertThat(leido).isEqualTo(suq301);
        assertThat(leido.nombre()).isEqualTo("Apartamento 301");
        assertThat(leido.capacidad()).isEqualTo(suq301.capacidad());
        assertThat(leido.admiteMascotas()).isTrue();
        assertThat(leido.advertenciaAcceso()).contains("Tercer piso sin ascensor");
        assertThat(leido.caracteristicas()).isEqualTo(suq301.caracteristicas());
        assertThat(leido.imagenes()).containsExactlyElementsOf(suq301.imagenes());
        assertThat(leido.estadoOperativo()).isEqualTo(EstadoOperativo.PREPARADO);
        assertThat(leido.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Apartamento · Un apartamento retirado sigue en la base pero no aparece en los listados (7.3)")
    void apartamentoRetiradoNoSeLista() {
        // Arrange
        Apartamento suq201 = FichaStaylyUq.suq201();
        Apartamento suq102 = FichaStaylyUq.suq102();
        suq102.retirar();

        // Act
        apartamentos.guardar(suq201);
        apartamentos.guardar(suq102);
        vaciarCache();

        // Assert
        assertThat(apartamentos.listarActivos()).containsExactly(suq201);
        assertThat(apartamentos.buscarPorIdentificacion(suq102.identificacion())).isPresent();
    }

    @Test
    @DisplayName("Temporada · Encuentra la temporada de cada noche y cae en la base cuando ninguna la contiene (F-05)")
    void buscarTemporadaDeUnaNoche() {
        // Arrange
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        vaciarCache();

        // Act + Assert
        assertThat(temporadas.buscarTemporadaDe(LocalDate.of(2026, 12, 20)).id()).isEqualTo(ID_ALTA);
        assertThat(temporadas.buscarTemporadaDe(LocalDate.of(2027, 1, 16)).id())
                .as("el 16 de enero ya no es alta: el periodo es [15-dic, 16-ene)").isEqualTo(ID_BASE);
        assertThat(temporadas.buscarTemporadaDe(LocalDate.of(2026, 11, 1)).id()).isEqualTo(ID_BASE);
        assertThat(temporadas.listarTodas()).hasSize(3);
    }

    @Test
    @DisplayName("Temporada · La estancia mínima de la base cambiada por el administrador se conserva (RP-01)")
    void conservarEstanciaMinimaDeLaBase() {
        // Arrange
        Temporada base = FichaStaylyUq.temporadaBase();
        base.cambiarEstanciaMinima(new EstanciaMinima(2));

        // Act
        temporadas.guardar(base);
        vaciarCache();

        // Assert
        Temporada leida = temporadas.buscarBase();
        assertThat(leida.esBase()).isTrue();
        assertThat(leida.estanciaMinima()).isEqualTo(new EstanciaMinima(2));
    }

    @Test
    @DisplayName("Tarifario · Conserva el histórico y devuelve la tarifa vigente más reciente (TA-2)")
    void conservarHistoricoDeTarifas() {
        // Arrange
        Tarifario tarifario = FichaStaylyUq.tarifarioSuq201();
        tarifario.definirTarifa(ID_BASE, Dinero.pesos(80_000), LocalDateTime.of(2026, 9, 1, 0, 0));

        // Act
        tarifarios.guardar(tarifario);
        vaciarCache();
        Tarifario leido = tarifarios.buscarPorApartamento(tarifario.apartamento()).orElseThrow();

        // Assert
        assertThat(leido.historico()).hasSize(4);
        assertThat(leido.tarifaVigente(ID_BASE).orElseThrow().valor()).isEqualTo(Dinero.pesos(80_000));
        assertThat(leido.tarifaVigente(ID_ALTA).orElseThrow().valor()).isEqualTo(Dinero.pesos(101_000));
    }

    /** Obliga a leer de H2 y no de la caché de primer nivel de JPA. */
    private void vaciarCache() {
        em.flush();
        em.clear();
    }
}

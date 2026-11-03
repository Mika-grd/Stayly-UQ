package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** La aplicación arranca con los adaptadores JPA: ningún puerto queda conectado a la memoria. */
@SpringBootTest
class ArranqueConAdaptadoresJpaTest {

    @Autowired
    private ApplicationContext contexto;
    @Autowired
    private ReservaRepository reservas;
    @Autowired
    private ApartamentoRepository apartamentos;
    @Autowired
    private ApartamentoJpaRepository apartamentoJpa;

    @Test
    @DisplayName("12.2 · Cada repositorio del dominio está implementado por su adaptador JPA")
    void cadaPuertoUsaSuAdaptadorJpa() {
        List<Class<?>> puertos = List.of(ReservaRepository.class, ApartamentoRepository.class,
                FolioRepository.class, BloqueoRepository.class, TemporadaRepository.class,
                TarifarioRepository.class, PoliticaCancelacionRepository.class, NovedadRepository.class,
                CanalRepository.class, ConflictoCanalRepository.class);

        assertThat(puertos).allSatisfy(puerto -> {
            Class<?> implementacion = AopUtils.getTargetClass(contexto.getBean(puerto));
            assertThat(implementacion.getPackageName()).endsWith("persistence.jpa.adapter");
            assertThat(implementacion.getSimpleName()).endsWith("RepositoryJpa");
        });
    }

    @Test
    @DisplayName("12.2 · Lo que se guarda por el puerto queda en H2 y el consecutivo funciona fuera de una prueba")
    void guardarPorElPuertoLlegaAH2() {
        // Arrange
        Apartamento suq201 = FichaStaylyUq.suq201();

        // Act
        apartamentos.guardar(suq201);
        String primero = reservas.siguienteCodigo().valor();
        String segundo = reservas.siguienteCodigo().valor();

        // Assert
        assertThat(apartamentoJpa.existsById("SUQ-201")).isTrue();
        assertThat(primero).matches("RES-\\d{4}-\\d{5}");
        assertThat(segundo).isNotEqualTo(primero);
    }
}

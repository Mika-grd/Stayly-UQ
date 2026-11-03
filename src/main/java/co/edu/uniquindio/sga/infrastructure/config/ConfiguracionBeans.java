package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.AltaApartamentoService;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.service.RegistroBloqueoService;
import co.edu.uniquindio.sga.domain.service.RetiroApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorEstanciaMinimaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ApartamentoRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.BloqueoRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.CanalRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ConflictoCanalRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.FolioRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.NovedadRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.PoliticaCancelacionRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.ReservaRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.TarifarioRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter.TemporadaRepositoryJpa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.BloqueoJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CanalJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConflictoCanalJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConsecutivoReservaJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.FolioJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.NovedadJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.PoliticaCancelacionJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TarifarioJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TemporadaJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Ensambla el hexágono: el dominio no conoce Spring, así que sus servicios y los adaptadores de
 * sus puertos se registran aquí como {@code @Bean}. Los casos de uso son {@code @Service} de la
 * capa de aplicación. Los repositorios son los adaptadores JPA sobre H2; las implementaciones en
 * memoria quedan solo para las pruebas unitarias.
 */
@Configuration
public class ConfiguracionBeans {

    /** 3.1: todas las fechas en la zona horaria de Colombia, sin importar la del servidor. */
    @Bean
    Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    @Bean
    ConfiguracionAlojamiento configuracionAlojamiento(PropiedadesAlojamiento propiedades) {
        return new ConfiguracionAlojamientoEnMemoria(propiedades);
    }

    // Repositorios: adaptadores JPA sobre H2

    @Bean
    ReservaRepository reservaRepository(ReservaJpaRepository jpa, ConsecutivoReservaJpaRepository consecutivos,
                                        Clock reloj) {
        return new ReservaRepositoryJpa(jpa, consecutivos, reloj);
    }

    @Bean
    ApartamentoRepository apartamentoRepository(ApartamentoJpaRepository jpa) {
        return new ApartamentoRepositoryJpa(jpa);
    }

    @Bean
    FolioRepository folioRepository(FolioJpaRepository jpa) {
        return new FolioRepositoryJpa(jpa);
    }

    @Bean
    BloqueoRepository bloqueoRepository(BloqueoJpaRepository jpa) {
        return new BloqueoRepositoryJpa(jpa);
    }

    @Bean
    TemporadaRepository temporadaRepository(TemporadaJpaRepository jpa) {
        return new TemporadaRepositoryJpa(jpa);
    }

    @Bean
    TarifarioRepository tarifarioRepository(TarifarioJpaRepository jpa) {
        return new TarifarioRepositoryJpa(jpa);
    }

    @Bean
    PoliticaCancelacionRepository politicaCancelacionRepository(PoliticaCancelacionJpaRepository jpa) {
        return new PoliticaCancelacionRepositoryJpa(jpa);
    }

    @Bean
    NovedadRepository novedadRepository(NovedadJpaRepository jpa) {
        return new NovedadRepositoryJpa(jpa);
    }

    @Bean
    CanalRepository canalRepository(CanalJpaRepository jpa) {
        return new CanalRepositoryJpa(jpa);
    }

    @Bean
    ConflictoCanalRepository conflictoCanalRepository(ConflictoCanalJpaRepository jpa) {
        return new ConflictoCanalRepositoryJpa(jpa);
    }

    // Servicios de dominio

    @Bean
    DisponibilidadApartamentoService disponibilidadApartamentoService(ReservaRepository reservas,
                                                                      BloqueoRepository bloqueos,
                                                                      ConfiguracionAlojamiento configuracion) {
        return new DisponibilidadApartamentoService(reservas, bloqueos, configuracion);
    }

    @Bean
    CotizadorEstanciaService cotizadorEstanciaService(TemporadaRepository temporadas, TarifarioRepository tarifarios,
                                                      ConfiguracionAlojamiento configuracion) {
        return new CotizadorEstanciaService(temporadas, tarifarios, configuracion);
    }

    @Bean
    ConfirmacionReservaService confirmacionReservaService(ConfiguracionAlojamiento configuracion) {
        return new ConfirmacionReservaService(configuracion);
    }

    @Bean
    EntregaApartamentoService entregaApartamentoService() {
        return new EntregaApartamentoService();
    }

    @Bean
    CalculadorRetencionService calculadorRetencionService(PoliticaCancelacionRepository politicas) {
        return new CalculadorRetencionService(politicas);
    }

    @Bean
    VerificadorTarifasCompletasService verificadorTarifasCompletasService(TemporadaRepository temporadas,
                                                                          TarifarioRepository tarifarios) {
        return new VerificadorTarifasCompletasService(temporadas, tarifarios);
    }

    @Bean
    VerificadorEstanciaMinimaService verificadorEstanciaMinimaService(TemporadaRepository temporadas) {
        return new VerificadorEstanciaMinimaService(temporadas);
    }

    @Bean
    AdmisionMascotasService admisionMascotasService(ConfiguracionAlojamiento configuracion) {
        return new AdmisionMascotasService(configuracion);
    }

    @Bean
    CalendarioTemporadasService calendarioTemporadasService(TemporadaRepository temporadas) {
        return new CalendarioTemporadasService(temporadas);
    }

    @Bean
    RegistroBloqueoService registroBloqueoService(ReservaRepository reservas) {
        return new RegistroBloqueoService(reservas);
    }

    @Bean
    RetiroApartamentoService retiroApartamentoService(ReservaRepository reservas) {
        return new RetiroApartamentoService(reservas);
    }

    @Bean
    AltaApartamentoService altaApartamentoService(ApartamentoRepository apartamentos) {
        return new AltaApartamentoService(apartamentos);
    }
}

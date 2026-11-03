package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
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
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
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

/**
 * Ensambla el hexágono: el dominio y la capa de aplicación no conocen Spring, así que sus
 * objetos se crean aquí. Los repositorios son los adaptadores JPA sobre H2; las
 * implementaciones en memoria quedan solo para las pruebas unitarias.
 */
@Configuration
public class ConfiguracionBeans {

    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
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

    // Casos de uso

    @Bean
    CrearReservaUseCase crearReservaUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas,
                                            FolioRepository folios, PoliticaCancelacionRepository politicas,
                                            VerificadorTarifasCompletasService verificadorTarifas,
                                            VerificadorEstanciaMinimaService verificadorEstanciaMinima,
                                            AdmisionMascotasService admisionMascotas,
                                            DisponibilidadApartamentoService disponibilidad,
                                            CotizadorEstanciaService cotizador, ConfiguracionAlojamiento configuracion,
                                            Clock reloj) {
        return new CrearReservaUseCase(apartamentos, reservas, folios, politicas, verificadorTarifas,
                verificadorEstanciaMinima, admisionMascotas, disponibilidad, cotizador, configuracion, reloj);
    }

    @Bean
    ConfirmarReservaUseCase confirmarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                                    ConfirmacionReservaService confirmacion, Clock reloj) {
        return new ConfirmarReservaUseCase(reservas, folios, confirmacion, reloj);
    }

    @Bean
    CancelarReservaUseCase cancelarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                                  CalculadorRetencionService calculadorRetencion, Clock reloj) {
        return new CancelarReservaUseCase(reservas, folios, calculadorRetencion, reloj);
    }

    @Bean
    RegistrarLlegadaUseCase registrarLlegadaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
                                                    EntregaApartamentoService entrega, Clock reloj) {
        return new RegistrarLlegadaUseCase(reservas, apartamentos, entrega, reloj);
    }
}

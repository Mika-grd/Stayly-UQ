package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
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
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.PoliticaCancelacionRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Ensambla el hexágono: el dominio y la capa de aplicación no conocen Spring, así que sus
 * objetos se crean aquí. Los repositorios son, por ahora, las implementaciones en memoria.
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

    // Repositorios

    @Bean
    ReservaRepository reservaRepository(Clock reloj) {
        return new ReservaRepositoryEnMemoria(reloj);
    }

    @Bean
    ApartamentoRepository apartamentoRepository() {
        return new ApartamentoRepositoryEnMemoria();
    }

    @Bean
    FolioRepository folioRepository() {
        return new FolioRepositoryEnMemoria();
    }

    @Bean
    BloqueoRepository bloqueoRepository() {
        return new BloqueoRepositoryEnMemoria();
    }

    @Bean
    TemporadaRepository temporadaRepository() {
        return new TemporadaRepositoryEnMemoria();
    }

    @Bean
    TarifarioRepository tarifarioRepository() {
        return new TarifarioRepositoryEnMemoria();
    }

    @Bean
    PoliticaCancelacionRepository politicaCancelacionRepository() {
        return new PoliticaCancelacionRepositoryEnMemoria();
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

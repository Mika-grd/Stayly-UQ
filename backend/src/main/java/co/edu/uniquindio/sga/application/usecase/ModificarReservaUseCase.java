package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase.DatosOcupante;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorEstanciaMinimaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CU-13 (RN-14): cambia apartamento, fechas, ocupantes o mascotas de una reserva PENDIENTE o
 * CONFIRMADA no iniciada. Repite las validaciones de CU-02 (la disponibilidad excluye a la propia
 * reserva), recalcula con las tarifas vigentes y deja la diferencia como ajuste en el folio. La
 * política congelada no cambia (RN-22). El cargo por mascotas se corrige con su movimiento
 * inverso (RN-16) y el nuevo cargo.
 */
@Service
@Transactional
public class ModificarReservaUseCase {

    private static final String CONCEPTO_MASCOTAS = "Cargo por mascotas";

    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final FolioRepository folios;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final VerificadorEstanciaMinimaService verificadorEstanciaMinima;
    private final AdmisionMascotasService admisionMascotas;
    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public ModificarReservaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
                                   FolioRepository folios, VerificadorTarifasCompletasService verificadorTarifas,
                                   VerificadorEstanciaMinimaService verificadorEstanciaMinima,
                                   AdmisionMascotasService admisionMascotas,
                                   DisponibilidadApartamentoService disponibilidad, CotizadorEstanciaService cotizador,
                                   ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = reservas;
        this.apartamentos = apartamentos;
        this.folios = folios;
        this.verificadorTarifas = verificadorTarifas;
        this.verificadorEstanciaMinima = verificadorEstanciaMinima;
        this.admisionMascotas = admisionMascotas;
        this.disponibilidad = disponibilidad;
        this.cotizador = cotizador;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public ReservaModificada ejecutar(Comando comando) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Reserva reserva = Consultas.reserva(reservas, comando.codigo());
        Folio folio = Consultas.folio(folios, comando.codigo());
        Apartamento apartamento = Consultas.apartamento(apartamentos, comando.apartamento());
        Estancia estancia = new Estancia(comando.entrada(), comando.salida());
        List<Ocupante> ocupantes = comando.ocupantes().stream().map(DatosOcupante::aOcupante).toList();
        MascotasAutorizadas mascotas = new MascotasAutorizadas(comando.mascotas());
        Dinero cargoMascotasAnterior = admisionMascotas.cargoPorMascotas(reserva.mascotas(), reserva.estancia());

        verificadorTarifas.verificarTarifasCompletas(apartamento.identificacion());
        verificadorEstanciaMinima.verificarEstanciaMinima(estancia);
        admisionMascotas.verificarMascotas(apartamento, mascotas);
        disponibilidad.verificarDisponible(apartamento, estancia, ocupantes.size(), reserva.codigo());

        ValorCongelado nuevoValor = cotizador.cotizar(apartamento.identificacion(), estancia,
                new ComposicionGrupo(ocupantes.stream().map(Ocupante::fechaNacimiento).toList()));
        Dinero diferencia = reserva.modificar(apartamento.identificacion(), estancia, ocupantes, mascotas, nuevoValor,
                configuracion.umbralEdad(), comando.autor(), ahora);
        folio.registrarAjuste(diferencia, ahora);
        Dinero cargoMascotasNuevo = admisionMascotas.cargoPorMascotas(mascotas, estancia);
        if (!cargoMascotasNuevo.equals(cargoMascotasAnterior)) {
            if (cargoMascotasAnterior.esPositivo()) {
                folio.registrarCargo(cargoMascotas(cargoMascotasAnterior, ahora).inverso(ahora));
            }
            if (cargoMascotasNuevo.esPositivo()) {
                folio.registrarCargo(cargoMascotas(cargoMascotasNuevo, ahora));
            }
        }

        reservas.guardar(reserva);
        folios.guardar(folio);
        return new ReservaModificada(reserva, folio, diferencia);
    }

    private static Cargo cargoMascotas(Dinero valor, LocalDateTime ahora) {
        return new Cargo(TipoCargo.SERVICIO_ADICIONAL, CONCEPTO_MASCOTAS, valor, ahora);
    }

    public record Comando(CodigoReserva codigo, IdentificacionApartamento apartamento, LocalDate entrada,
                          LocalDate salida, List<DatosOcupante> ocupantes, int mascotas, String autor) {
    }

    /** La reserva con su nuevo valor y la diferencia registrada como ajuste (positiva: el huésped debe más). */
    public record ReservaModificada(Reserva reserva, Folio folio, Dinero diferencia) {
    }
}

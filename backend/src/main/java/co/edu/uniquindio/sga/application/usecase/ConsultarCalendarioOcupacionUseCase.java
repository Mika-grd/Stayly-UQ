package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 7.5: calendario de ocupación por apartamento y rango [desde, hasta): cada noche está reservada
 * (por una reserva activa, RN-12), bloqueada (RN-07) o libre. Es una consulta, no decide nada.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarCalendarioOcupacionUseCase {

    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;
    private final BloqueoRepository bloqueos;

    public ConsultarCalendarioOcupacionUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas,
                                               BloqueoRepository bloqueos) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.bloqueos = bloqueos;
    }

    /** Sin apartamento: todos los activos. */
    public Calendario ejecutar(RangoFechas rango, IdentificacionApartamento apartamento) {
        List<Apartamento> consultados = apartamento == null
                ? apartamentos.listarActivos().stream()
                        .sorted(Comparator.comparing(ap -> ap.identificacion().valor()))
                        .toList()
                : List.of(Consultas.apartamento(apartamentos, apartamento));
        Estancia periodo = new Estancia(rango.inicio(), rango.fin());
        return new Calendario(rango, consultados.stream()
                .map(ap -> ocupacionDe(ap.identificacion(), periodo))
                .toList());
    }

    private OcupacionApartamento ocupacionDe(IdentificacionApartamento apartamento, Estancia periodo) {
        List<Reserva> activas = reservas.buscarActivasQueSolapan(apartamento, periodo);
        List<Bloqueo> vigentes = bloqueos.buscarVigentesQueSolapan(apartamento, periodo);
        List<NocheOcupacion> noches = periodo.fechasDeNoches().stream()
                .map(noche -> nocheDe(noche, activas, vigentes))
                .toList();
        return new OcupacionApartamento(apartamento, noches);
    }

    private static NocheOcupacion nocheDe(LocalDate noche, List<Reserva> activas, List<Bloqueo> vigentes) {
        Optional<Reserva> reserva = activas.stream().filter(r -> r.estancia().contiene(noche)).findFirst();
        if (reserva.isPresent()) {
            return new NocheOcupacion(noche, EstadoNoche.RESERVADA, reserva.get().codigo());
        }
        boolean bloqueada = vigentes.stream().anyMatch(bloqueo -> bloqueo.rango().contiene(noche));
        return new NocheOcupacion(noche, bloqueada ? EstadoNoche.BLOQUEADA : EstadoNoche.LIBRE, null);
    }

    public enum EstadoNoche { LIBRE, RESERVADA, BLOQUEADA }

    public record NocheOcupacion(LocalDate fecha, EstadoNoche estado, CodigoReserva reserva) {
    }

    public record OcupacionApartamento(IdentificacionApartamento apartamento, List<NocheOcupacion> noches) {
    }

    public record Calendario(RangoFechas rango, List<OcupacionApartamento> apartamentos) {
    }
}

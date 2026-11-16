package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ReservaRepositoryEnMemoria implements ReservaRepository {

    private final Map<CodigoReserva, Reserva> reservas = new HashMap<>();
    private final AtomicInteger consecutivo = new AtomicInteger();
    private final Clock reloj;

    public ReservaRepositoryEnMemoria(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public void guardar(Reserva reserva) {
        reservas.put(reserva.codigo(), reserva);
    }

    @Override
    public Optional<Reserva> buscarPorCodigo(CodigoReserva codigo) {
        return Optional.ofNullable(reservas.get(codigo));
    }

    @Override
    public List<Reserva> buscarActivasQueSolapan(IdentificacionApartamento apartamento, Estancia estancia) {
        return reservas.values().stream()
                .filter(reserva -> reserva.apartamento().equals(apartamento))
                .filter(Reserva::estaActiva)
                .filter(reserva -> reserva.estancia().seSolapaCon(estancia))
                .toList();
    }

    @Override
    public Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador) {
        return reservas.values().stream()
                .filter(reserva -> reserva.identificadorExterno().filter(identificador::equals).isPresent())
                .findFirst();
    }

    @Override
    public List<Reserva> buscarPendientesCreadasAntesDe(LocalDateTime limite) {
        return reservas.values().stream()
                .filter(reserva -> reserva.estado() == EstadoReserva.PENDIENTE)
                .filter(reserva -> reserva.fechaCreacion().isBefore(limite))
                .toList();
    }

    @Override
    public boolean tieneActivasOFuturas(IdentificacionApartamento apartamento, LocalDate hoy) {
        return reservas.values().stream()
                .filter(reserva -> reserva.apartamento().equals(apartamento))
                .anyMatch(reserva -> reserva.estaActiva() && reserva.estancia().salida().isAfter(hoy));
    }

    @Override
    public List<Reserva> buscarActivasOFuturas(IdentificacionApartamento apartamento, LocalDate hoy) {
        return reservas.values().stream()
                .filter(reserva -> reserva.apartamento().equals(apartamento))
                .filter(reserva -> reserva.estaActiva() && reserva.estancia().salida().isAfter(hoy))
                .toList();
    }

    @Override
    public List<Reserva> buscarConEntradaEl(LocalDate fecha) {
        return reservas.values().stream().filter(r -> r.estancia().entrada().equals(fecha))
                .sorted(Comparator.comparing(r -> r.apartamento().valor())).toList();
    }

    @Override
    public List<Reserva> buscarConSalidaEl(LocalDate fecha) {
        return reservas.values().stream().filter(r -> r.estancia().salida().equals(fecha))
                .sorted(Comparator.comparing(r -> r.apartamento().valor())).toList();
    }

    @Override
    public Pagina<Reserva> buscarPorDocumentoTitular(String documento, PeticionPagina pagina) {
        return Pagina.de(reservas.values().stream()
                .filter(r -> r.titular().documento().map(doc -> doc.numero().equals(documento)).orElse(false))
                .sorted(Comparator.comparing(Reserva::fechaCreacion).reversed())
                .toList(), pagina);
    }

    @Override
    public List<Reserva> buscarQueSolapan(RangoFechas rango) {
        return reservas.values().stream().filter(r -> rango.seSolapaCon(r.estancia())).toList();
    }

    @Override
    public List<Reserva> buscarCreadasEntre(LocalDateTime desde, LocalDateTime hasta) {
        return reservas.values().stream()
                .filter(r -> !r.fechaCreacion().isBefore(desde) && r.fechaCreacion().isBefore(hasta))
                .toList();
    }

    @Override
    public Pagina<Reserva> buscar(FiltroReservas filtro, PeticionPagina pagina) {
        List<Reserva> filtradas = reservas.values().stream()
                .filter(filtro::admite)
                .sorted(Comparator.comparing(Reserva::fechaCreacion)
                        .thenComparing(reserva -> reserva.codigo().valor())
                        .reversed())
                .toList();
        return Pagina.de(filtradas, pagina);
    }

    @Override
    public CodigoReserva siguienteCodigo() {
        return new CodigoReserva("RES-%d-%05d".formatted(Year.now(reloj).getValue(), consecutivo.incrementAndGet()));
    }
}

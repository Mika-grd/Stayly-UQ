package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
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
    public CodigoReserva siguienteCodigo() {
        return new CodigoReserva("RES-%d-%05d".formatted(Year.now(reloj).getValue(), consecutivo.incrementAndGet()));
    }
}

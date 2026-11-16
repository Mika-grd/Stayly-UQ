package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * CU-42 (7.1, 7.6): llegadas y salidas previstas de un día (por defecto hoy). Llegadas: reservas que
 * entran ese día y siguen vigentes (CONFIRMADA, o EN_CURSO si el grupo ya llegó). Salidas: reservas que
 * salen ese día y están EN_CURSO, o FINALIZADA si el grupo ya salió.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarMovimientosDelDiaUseCase {

    private static final Set<EstadoReserva> LLEGADAS = Set.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);
    private static final Set<EstadoReserva> SALIDAS = Set.of(EstadoReserva.EN_CURSO, EstadoReserva.FINALIZADA);

    private final ReservaRepository reservas;
    private final Clock reloj;

    public ConsultarMovimientosDelDiaUseCase(ReservaRepository reservas, Clock reloj) {
        this.reservas = reservas;
        this.reloj = reloj;
    }

    public Movimientos ejecutar(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(reloj) : fecha;
        return new Movimientos(dia,
                reservas.buscarConEntradaEl(dia).stream().filter(r -> LLEGADAS.contains(r.estado())).toList(),
                reservas.buscarConSalidaEl(dia).stream().filter(r -> SALIDAS.contains(r.estado())).toList());
    }

    public record Movimientos(LocalDate fecha, List<Reserva> llegadas, List<Reserva> salidas) {
    }
}

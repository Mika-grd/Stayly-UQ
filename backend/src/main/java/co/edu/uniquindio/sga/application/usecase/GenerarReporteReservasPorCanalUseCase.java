package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte de reservas por canal: las reservas creadas en los días [desde, hasta) por cada canal, con
 * cuántas terminaron CANCELADA y cuántas NO_SHOW. Aparecen los tres canales aunque no tengan reservas.
 */
@Service
@Transactional(readOnly = true)
public class GenerarReporteReservasPorCanalUseCase {

    private final ReservaRepository reservas;

    public GenerarReporteReservasPorCanalUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public ReporteReservasPorCanal ejecutar(LocalDate desde, LocalDate hasta) {
        new RangoFechas(desde, hasta);
        List<Reserva> creadas = reservas.buscarCreadasEntre(desde.atStartOfDay(), hasta.atStartOfDay());
        List<ReservasCanal> porCanal = Arrays.stream(CanalOrigen.values())
                .map(canal -> {
                    List<Reserva> delCanal = creadas.stream().filter(reserva -> reserva.canal() == canal).toList();
                    return new ReservasCanal(canal, delCanal.size(), contar(delCanal, EstadoReserva.CANCELADA),
                            contar(delCanal, EstadoReserva.NO_SHOW));
                })
                .toList();
        return new ReporteReservasPorCanal(desde, hasta, porCanal);
    }

    private static int contar(List<Reserva> reservas, EstadoReserva estado) {
        return (int) reservas.stream().filter(reserva -> reserva.estado() == estado).count();
    }

    public record ReporteReservasPorCanal(LocalDate desde, LocalDate hasta, List<ReservasCanal> porCanal) {
    }

    public record ReservasCanal(CanalOrigen canal, int total, int canceladas, int noShow) {
    }
}

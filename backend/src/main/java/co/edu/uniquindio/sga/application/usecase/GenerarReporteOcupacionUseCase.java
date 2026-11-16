package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reporte de ocupación: noches vendidas sobre noches disponibles de los apartamentos activos en las
 * noches [desde, hasta). Una noche está vendida si es de una reserva CONFIRMADA, EN_CURSO o FINALIZADA;
 * las PENDIENTE aún no son venta y las CANCELADA o NO_SHOW no ocuparon el apartamento.
 */
@Service
@Transactional(readOnly = true)
public class GenerarReporteOcupacionUseCase {

    private static final Set<EstadoReserva> VENDIDAS =
            EnumSet.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO, EstadoReserva.FINALIZADA);

    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;

    public GenerarReporteOcupacionUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
    }

    public ReporteOcupacion ejecutar(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = new RangoFechas(desde, hasta);
        Set<IdentificacionApartamento> activos = apartamentos.listarActivos().stream()
                .map(Apartamento::identificacion)
                .collect(Collectors.toSet());
        int nochesPeriodo = (int) ChronoUnit.DAYS.between(desde, hasta);
        int nochesVendidas = (int) reservas.buscarQueSolapan(rango).stream()
                .filter(reserva -> VENDIDAS.contains(reserva.estado()))
                .filter(reserva -> activos.contains(reserva.apartamento()))
                .flatMap(reserva -> reserva.estancia().fechasDeNoches().stream())
                .filter(rango::contiene)
                .count();
        int disponibles = activos.size() * nochesPeriodo;
        double proporcion = disponibles == 0 ? 0 : Math.round(10_000.0 * nochesVendidas / disponibles) / 10_000.0;
        return new ReporteOcupacion(desde, hasta, activos.size(), nochesPeriodo, nochesVendidas, proporcion);
    }

    /** {@code porcentajeOcupacion} es una proporción entre 0 y 1, con cuatro decimales. */
    public record ReporteOcupacion(LocalDate desde, LocalDate hasta, int apartamentosActivos, int nochesPeriodo,
                                   int nochesVendidas, double porcentajeOcupacion) {
    }
}

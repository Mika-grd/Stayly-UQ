package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte de ingresos: la suma de los pagos registrados en los días [desde, hasta), por medio de pago.
 * Los cargos no pagados no cuentan; un pago negativo (corrección, RN-16) resta.
 */
@Service
@Transactional(readOnly = true)
public class GenerarReporteIngresosUseCase {

    private final FolioRepository folios;

    public GenerarReporteIngresosUseCase(FolioRepository folios) {
        this.folios = folios;
    }

    public ReporteIngresos ejecutar(LocalDate desde, LocalDate hasta) {
        new RangoFechas(desde, hasta);
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atStartOfDay();
        List<Pago> pagos = folios.buscarConPagosEntre(inicio, fin).stream()
                .flatMap(folio -> folio.pagos().stream())
                .filter(pago -> !pago.fecha().isBefore(inicio) && pago.fecha().isBefore(fin))
                .toList();
        List<IngresoPorMedio> porMedio = Arrays.stream(MedioPago.values())
                .map(medio -> new IngresoPorMedio(medio, sumar(pagos.stream()
                        .filter(pago -> pago.medio() == medio).toList())))
                .toList();
        return new ReporteIngresos(desde, hasta, sumar(pagos), porMedio);
    }

    private static Dinero sumar(List<Pago> pagos) {
        return pagos.stream().map(Pago::valor).reduce(Dinero.cero(), Dinero::sumar);
    }

    public record ReporteIngresos(LocalDate desde, LocalDate hasta, Dinero total, List<IngresoPorMedio> porMedioPago) {
    }

    public record IngresoPorMedio(MedioPago medio, Dinero total) {
    }
}

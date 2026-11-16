package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.IngresoPorMedioDTO;
import co.edu.uniquindio.sga.application.dto.response.ReporteIngresosResponse;
import co.edu.uniquindio.sga.application.dto.response.ReporteOcupacionResponse;
import co.edu.uniquindio.sga.application.dto.response.ReporteReservasPorCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservasCanalDTO;
import co.edu.uniquindio.sga.application.usecase.GenerarReporteIngresosUseCase;
import co.edu.uniquindio.sga.application.usecase.GenerarReporteOcupacionUseCase;
import co.edu.uniquindio.sga.application.usecase.GenerarReporteReservasPorCanalUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static co.edu.uniquindio.sga.infrastructure.web.mapper.DominioWebMapperSoporte.dineroALong;

/**
 * Reportes del administrador. El rango es de días [desde, hasta), como el resto de rangos del sistema;
 * {@code hasta} debe ser posterior a {@code desde} (RANGO_FECHAS_INVALIDO, 3.1).
 */
@RestController
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class ReporteController {

    private final GenerarReporteOcupacionUseCase ocupacion;
    private final GenerarReporteIngresosUseCase ingresos;
    private final GenerarReporteReservasPorCanalUseCase reservasPorCanal;

    public ReporteController(GenerarReporteOcupacionUseCase ocupacion, GenerarReporteIngresosUseCase ingresos,
                             GenerarReporteReservasPorCanalUseCase reservasPorCanal) {
        this.ocupacion = ocupacion;
        this.ingresos = ingresos;
        this.reservasPorCanal = reservasPorCanal;
    }

    @GetMapping("/api/reportes/ocupacion")
    public ReporteOcupacionResponse ocupacion(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        var reporte = ocupacion.ejecutar(desde, hasta);
        return new ReporteOcupacionResponse(reporte.desde(), reporte.hasta(), reporte.apartamentosActivos(),
                reporte.nochesPeriodo(), reporte.nochesVendidas(), reporte.porcentajeOcupacion());
    }

    @GetMapping("/api/reportes/ingresos")
    public ReporteIngresosResponse ingresos(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        var reporte = ingresos.ejecutar(desde, hasta);
        return new ReporteIngresosResponse(reporte.desde(), reporte.hasta(), dineroALong(reporte.total()),
                reporte.porMedioPago().stream()
                        .map(medio -> new IngresoPorMedioDTO(medio.medio(), dineroALong(medio.total())))
                        .toList());
    }

    @GetMapping("/api/reportes/reservas-por-canal")
    public ReporteReservasPorCanalResponse reservasPorCanal(@RequestParam LocalDate desde,
                                                            @RequestParam LocalDate hasta) {
        var reporte = reservasPorCanal.ejecutar(desde, hasta);
        return new ReporteReservasPorCanalResponse(reporte.desde(), reporte.hasta(), reporte.porCanal().stream()
                .map(canal -> new ReservasCanalDTO(canal.canal(), canal.total(), canal.canceladas(), canal.noShow()))
                .toList());
    }
}

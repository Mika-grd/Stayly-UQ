package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.CalendarioOcupacionResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.infrastructure.web.mapper.OperacionDiariaWebMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller delgado del calendario de ocupación (CU-35, 7.1): recepción y administración. */
@RestController
public class CalendarioOcupacionController {

    private final ConsultarCalendarioOcupacionUseCase consultar;
    private final OperacionDiariaWebMapper mapper;

    public CalendarioOcupacionController(ConsultarCalendarioOcupacionUseCase consultar,
                                         OperacionDiariaWebMapper mapper) {
        this.consultar = consultar;
        this.mapper = mapper;
    }

    @GetMapping("/api/calendario-ocupacion")
    @PreAuthorize("hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')")
    public CalendarioOcupacionResponse consultar(@RequestParam LocalDate desde, @RequestParam LocalDate hasta,
                                                 @RequestParam(required = false) String apartamento) {
        IdentificacionApartamento id = apartamento == null ? null : new IdentificacionApartamento(apartamento);
        return mapper.aCalendario(consultar.ejecutar(new RangoFechas(desde, hasta), id));
    }
}

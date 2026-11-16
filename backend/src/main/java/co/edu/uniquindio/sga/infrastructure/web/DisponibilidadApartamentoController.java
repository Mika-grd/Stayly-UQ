package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.DisponibilidadApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.DisponibilidadApartamentoResponse.NocheDisponibleDTO;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadApartamentoUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller delgado del calendario de disponibilidad del detalle (7.1): lectura pública del catálogo. */
@RestController
public class DisponibilidadApartamentoController {

    private final ConsultarDisponibilidadApartamentoUseCase consultar;

    public DisponibilidadApartamentoController(ConsultarDisponibilidadApartamentoUseCase consultar) {
        this.consultar = consultar;
    }

    @GetMapping("/api/apartamentos/{identificacion}/disponibilidad")
    public DisponibilidadApartamentoResponse consultar(@PathVariable String identificacion,
                                                       @RequestParam LocalDate desde,
                                                       @RequestParam LocalDate hasta) {
        var calendario = consultar.ejecutar(new IdentificacionApartamento(identificacion), new RangoFechas(desde, hasta));
        return new DisponibilidadApartamentoResponse(calendario.apartamento().valor(), calendario.rango().inicio(),
                calendario.rango().fin(), calendario.noches().stream()
                .map(noche -> new NocheDisponibleDTO(noche.fecha(), noche.disponible()))
                .toList());
    }
}

package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CotizacionRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.infrastructure.web.mapper.CotizacionWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller delgado de la Cotización (CU-01): valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class CotizacionController {

    private final CotizarEstanciaUseCase cotizar;
    private final CotizacionWebMapper mapper;

    public CotizacionController(CotizarEstanciaUseCase cotizar, CotizacionWebMapper mapper) {
        this.cotizar = cotizar;
        this.mapper = mapper;
    }

    @PostMapping("/api/cotizaciones")
    public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest request) {
        int mascotas = request.mascotasAutorizadas() == null ? 0 : request.mascotasAutorizadas();
        var cotizacion = cotizar.ejecutar(new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(), request.fechaSalida(), request.fechasNacimientoOcupantes(),
                new MascotasAutorizadas(mascotas));
        return mapper.aResponse(cotizacion);
    }
}

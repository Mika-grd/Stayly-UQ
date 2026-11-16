package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.PoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.usecase.ActualizarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarPoliticaVigenteUseCase;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.infrastructure.web.mapper.PoliticaCancelacionWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Controller delgado del recurso Política de cancelación: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class PoliticaCancelacionController {

    private final ActualizarPoliticaCancelacionUseCase actualizar;
    private final ConsultarPoliticaVigenteUseCase consultarVigente;
    private final PoliticaCancelacionWebMapper mapper;

    public PoliticaCancelacionController(ActualizarPoliticaCancelacionUseCase actualizar,
                                         ConsultarPoliticaVigenteUseCase consultarVigente,
                                         PoliticaCancelacionWebMapper mapper) {
        this.actualizar = actualizar;
        this.consultarVigente = consultarVigente;
        this.mapper = mapper;
    }

    @PostMapping("/api/politica-cancelacion/versiones")
    public ResponseEntity<PoliticaCancelacionResponse> actualizar(@Valid @RequestBody PoliticaCancelacionRequest request,
                                                                  UriComponentsBuilder uri) {
        PoliticaCancelacion politica = actualizar.ejecutar(mapper.aTramos(request), request.porcentajeRetencionNoShow());
        var ubicacion = uri.path("/api/politica-cancelacion/vigente").build().toUri();
        return ResponseEntity.created(ubicacion).body(mapper.aResponse(politica));
    }

    @GetMapping("/api/politica-cancelacion/vigente")
    public PoliticaCancelacionResponse consultarVigente() {
        return mapper.aResponse(consultarVigente.ejecutar());
    }
}

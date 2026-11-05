package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.BloqueoRequest;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.usecase.RegistrarBloqueoUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.infrastructure.web.mapper.BloqueoWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Controller delgado del recurso Bloqueo (CU-16): valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class BloqueoController {

    private final RegistrarBloqueoUseCase registrar;
    private final BloqueoWebMapper mapper;

    public BloqueoController(RegistrarBloqueoUseCase registrar, BloqueoWebMapper mapper) {
        this.registrar = registrar;
        this.mapper = mapper;
    }

    @PostMapping("/api/apartamentos/{identificacion}/bloqueos")
    public ResponseEntity<BloqueoResponse> registrar(@PathVariable String identificacion,
                                                     @Valid @RequestBody BloqueoRequest request,
                                                     UriComponentsBuilder uri) {
        RangoFechas rango = new RangoFechas(request.fechaInicio(), request.fechaFin());
        Bloqueo bloqueo = registrar.ejecutar(new IdentificacionApartamento(identificacion), rango, request.motivo(),
                "Administrador");
        var ubicacion = uri.path("/api/apartamentos/{identificacion}/bloqueos").buildAndExpand(identificacion).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.aResponse(bloqueo));
    }
}

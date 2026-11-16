package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.TarifarioResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarTarifasUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.web.mapper.TarifaWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller delgado del recurso Tarifas: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class TarifaController {

    private final GestionarTarifasUseCase gestionar;
    private final TarifaWebMapper mapper;

    public TarifaController(GestionarTarifasUseCase gestionar, TarifaWebMapper mapper) {
        this.gestionar = gestionar;
        this.mapper = mapper;
    }

    @PutMapping("/api/apartamentos/{identificacion}/tarifas")
    public TarifarioResponse definir(@PathVariable String identificacion, @Valid @RequestBody TarifaRequest request) {
        var resultado = gestionar.definir(new IdentificacionApartamento(identificacion), mapper.aTarifasDefinidas(request));
        return mapper.aTarifarioResponse(resultado);
    }

    @GetMapping("/api/apartamentos/{identificacion}/tarifas")
    public TarifarioResponse consultar(@PathVariable String identificacion) {
        var resultado = gestionar.consultar(new IdentificacionApartamento(identificacion));
        return mapper.aTarifarioResponse(resultado);
    }
}

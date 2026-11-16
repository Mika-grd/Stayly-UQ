package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.EstanciaMinimaRequest;
import co.edu.uniquindio.sga.application.dto.request.TemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.ConfigurarEstanciaMinimaUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasUseCase;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.infrastructure.web.mapper.TemporadaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/** Controller delgado del recurso Temporada: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class TemporadaController {

    private final GestionarTemporadasUseCase gestionar;
    private final ConfigurarEstanciaMinimaUseCase configurarEstanciaMinima;
    private final TemporadaWebMapper mapper;

    public TemporadaController(GestionarTemporadasUseCase gestionar,
                               ConfigurarEstanciaMinimaUseCase configurarEstanciaMinima, TemporadaWebMapper mapper) {
        this.gestionar = gestionar;
        this.configurarEstanciaMinima = configurarEstanciaMinima;
        this.mapper = mapper;
    }

    @PostMapping("/api/temporadas")
    public ResponseEntity<TemporadaResponse> crear(@Valid @RequestBody TemporadaRequest request,
                                                   UriComponentsBuilder uri) {
        Temporada temporada = gestionar.crear(mapper.aDatos(request));
        var ubicacion = uri.path("/api/temporadas/{id}").buildAndExpand(temporada.id().valor()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.aResponse(temporada));
    }

    @GetMapping("/api/temporadas")
    public List<TemporadaResponse> listar() {
        return gestionar.listar().stream().map(mapper::aResponse).toList();
    }

    @PutMapping("/api/temporadas/{id}")
    public TemporadaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody TemporadaRequest request) {
        Temporada temporada = gestionar.actualizar(new IdTemporada(id), mapper.aDatos(request));
        return mapper.aResponse(temporada);
    }

    @PutMapping("/api/temporadas/{id}/estancia-minima")
    public TemporadaResponse configurarEstanciaMinima(@PathVariable UUID id,
                                                      @Valid @RequestBody EstanciaMinimaRequest request) {
        Temporada temporada = configurarEstanciaMinima.ejecutar(new IdTemporada(id),
                new EstanciaMinima(request.estanciaMinima()));
        return mapper.aResponse(temporada);
    }
}

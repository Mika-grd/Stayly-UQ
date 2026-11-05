package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.ApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.response.ActualizacionApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaApartamentoResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.UbicacionDTO;
import co.edu.uniquindio.sga.application.usecase.ActivarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ActualizarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase.Criterios;
import co.edu.uniquindio.sga.application.usecase.ConsultarDetalleApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDetalleApartamentoUseCase.DetalleApartamento;
import co.edu.uniquindio.sga.application.usecase.CrearApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.RetirarApartamentoUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.infrastructure.web.mapper.ApartamentoWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Controller delgado del recurso Apartamento: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
public class ApartamentoController {

    private final BuscarApartamentosDisponiblesUseCase buscarDisponibles;
    private final ConsultarDetalleApartamentoUseCase consultarDetalle;
    private final CrearApartamentoUseCase crear;
    private final ActualizarApartamentoUseCase actualizar;
    private final ActivarApartamentoUseCase activar;
    private final RetirarApartamentoUseCase retirar;
    private final ApartamentoWebMapper mapper;

    public ApartamentoController(BuscarApartamentosDisponiblesUseCase buscarDisponibles,
                                 ConsultarDetalleApartamentoUseCase consultarDetalle, CrearApartamentoUseCase crear,
                                 ActualizarApartamentoUseCase actualizar, ActivarApartamentoUseCase activar,
                                 RetirarApartamentoUseCase retirar, ApartamentoWebMapper mapper) {
        this.buscarDisponibles = buscarDisponibles;
        this.consultarDetalle = consultarDetalle;
        this.crear = crear;
        this.actualizar = actualizar;
        this.activar = activar;
        this.retirar = retirar;
        this.mapper = mapper;
    }

    @GetMapping("/api/apartamentos/disponibles")
    public PaginaApartamentoResumenResponse buscarDisponibles(@RequestParam LocalDate fechaEntrada,
            @RequestParam LocalDate fechaSalida, @RequestParam List<LocalDate> fechasNacimiento,
            @RequestParam(required = false) Integer capacidadMinima, @RequestParam(required = false) Long precioMaximo,
            @RequestParam(required = false) Set<String> caracteristicas,
            @RequestParam(defaultValue = "0") int pagina) {
        Set<Caracteristica> caracteristicasDominio = caracteristicas == null ? null
                : caracteristicas.stream().map(Caracteristica::new).collect(java.util.stream.Collectors.toSet());
        Dinero precioMaximoDominio = precioMaximo == null ? null : Dinero.pesos(precioMaximo);
        Criterios criterios = new Criterios(fechaEntrada, fechaSalida, fechasNacimiento, capacidadMinima,
                precioMaximoDominio, caracteristicasDominio);
        Pagina<BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible> resultado = buscarDisponibles
                .ejecutar(criterios, new PeticionPagina(pagina));
        List<ApartamentoResumenResponse> contenido = resultado.contenido().stream().map(mapper::aResumen).toList();
        return new PaginaApartamentoResumenResponse(contenido, resultado.numero(), resultado.tamano(),
                resultado.totalElementos(), resultado.totalPaginas());
    }

    @GetMapping("/api/apartamentos/{identificacion}")
    public ApartamentoDetalleResponse consultarDetalle(@PathVariable String identificacion) {
        DetalleApartamento detalle = consultarDetalle.ejecutar(new IdentificacionApartamento(identificacion));
        UbicacionDTO ubicacion = new UbicacionDTO(detalle.ubicacion().latitud(), detalle.ubicacion().longitud());
        return mapper.aDetalle(detalle.apartamento(), ubicacion);
    }

    @PostMapping("/api/apartamentos")
    public ResponseEntity<ApartamentoDetalleResponse> crear(@Valid @RequestBody ApartamentoRequest request,
                                                            UriComponentsBuilder uri) {
        IdentificacionApartamento identificacion = new IdentificacionApartamento(request.identificacion());
        Apartamento apartamento = crear.ejecutar(identificacion, mapper.aDatos(request));
        DetalleApartamento detalle = consultarDetalle.ejecutar(apartamento.identificacion());
        UbicacionDTO ubicacion = new UbicacionDTO(detalle.ubicacion().latitud(), detalle.ubicacion().longitud());
        var ubicacionRecurso = uri.path("/api/apartamentos/{identificacion}")
                .buildAndExpand(apartamento.identificacion().valor()).toUri();
        return ResponseEntity.created(ubicacionRecurso).body(mapper.aDetalle(apartamento, ubicacion));
    }

    @PutMapping("/api/apartamentos/{identificacion}")
    public ActualizacionApartamentoResponse actualizar(@PathVariable String identificacion,
                                                       @Valid @RequestBody ApartamentoRequest request) {
        var resultado = actualizar.ejecutar(new IdentificacionApartamento(identificacion), mapper.aDatos(request));
        DetalleApartamento detalle = consultarDetalle.ejecutar(resultado.apartamento().identificacion());
        UbicacionDTO ubicacion = new UbicacionDTO(detalle.ubicacion().latitud(), detalle.ubicacion().longitud());
        return mapper.aActualizacion(resultado, ubicacion);
    }

    @DeleteMapping("/api/apartamentos/{identificacion}")
    public ResponseEntity<Void> retirar(@PathVariable String identificacion) {
        retirar.ejecutar(new IdentificacionApartamento(identificacion));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/apartamentos/{identificacion}/activar")
    public ApartamentoDetalleResponse activar(@PathVariable String identificacion) {
        IdentificacionApartamento id = new IdentificacionApartamento(identificacion);
        Apartamento apartamento = activar.ejecutar(id);
        DetalleApartamento detalle = consultarDetalle.ejecutar(id);
        UbicacionDTO ubicacion = new UbicacionDTO(detalle.ubicacion().latitud(), detalle.ubicacion().longitud());
        return mapper.aDetalle(apartamento, ubicacion);
    }
}

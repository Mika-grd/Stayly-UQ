package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.NovedadRequest;
import co.edu.uniquindio.sga.application.dto.response.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaNovedadResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarNovedadesUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarNovedadUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import co.edu.uniquindio.sga.infrastructure.web.mapper.OperacionDiariaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Controller delgado de las novedades de un apartamento (CU-11, CU-41, 7.6). */
@RestController
public class NovedadController {

    private static final String OPERACION = "hasAnyAuthority('PERSONAL_SERVICIO', 'RECEPCIONISTA', 'ADMINISTRADOR')";

    private final RegistrarNovedadUseCase registrar;
    private final ConsultarNovedadesUseCase consultar;
    private final OperacionDiariaWebMapper mapper;

    public NovedadController(RegistrarNovedadUseCase registrar, ConsultarNovedadesUseCase consultar,
                             OperacionDiariaWebMapper mapper) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.mapper = mapper;
    }

    @PostMapping("/api/apartamentos/{identificacion}/novedades")
    @PreAuthorize(OPERACION)
    public ResponseEntity<NovedadResponse> registrar(@PathVariable String identificacion,
                                                     @Valid @RequestBody NovedadRequest request,
                                                     @AuthenticationPrincipal Jwt jwt, UriComponentsBuilder uri) {
        Novedad novedad = registrar.ejecutar(new IdentificacionApartamento(identificacion), request.descripcion(),
                request.gravedad(), UsuarioAutenticado.de(jwt).correo());
        return ResponseEntity.created(uri.path("/api/apartamentos/{identificacion}/novedades")
                        .buildAndExpand(identificacion).toUri())
                .body(mapper.aNovedad(novedad));
    }

    @GetMapping("/api/apartamentos/{identificacion}/novedades")
    @PreAuthorize(OPERACION)
    public PaginaNovedadResponse consultar(@PathVariable String identificacion,
                                           @RequestParam(defaultValue = "0") int pagina) {
        return mapper.aPaginaNovedades(consultar.ejecutar(new IdentificacionApartamento(identificacion),
                new PeticionPagina(pagina)));
    }
}

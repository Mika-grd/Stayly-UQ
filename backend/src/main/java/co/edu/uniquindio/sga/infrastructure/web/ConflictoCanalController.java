package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.ResolverConflictoRequest;
import co.edu.uniquindio.sga.application.dto.response.ConflictoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaConflictoCanalResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarConflictosCanalUseCase;
import co.edu.uniquindio.sga.application.usecase.ResolverConflictoCanalUseCase;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import co.edu.uniquindio.sga.infrastructure.web.mapper.CanalWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Revisión de los conflictos con canales externos (7.8, RN-18). Solo administrador. */
@RestController
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class ConflictoCanalController {

    private final ConsultarConflictosCanalUseCase consultar;
    private final ResolverConflictoCanalUseCase resolver;
    private final CanalWebMapper mapper;

    public ConflictoCanalController(ConsultarConflictosCanalUseCase consultar, ResolverConflictoCanalUseCase resolver,
                                    CanalWebMapper mapper) {
        this.consultar = consultar;
        this.resolver = resolver;
        this.mapper = mapper;
    }

    @GetMapping("/api/conflictos-canal")
    public PaginaConflictoCanalResponse consultar(@RequestParam(required = false) EstadoConflicto estado,
                                                  @RequestParam(defaultValue = "0") int pagina) {
        return mapper.aPaginaConflictos(consultar.ejecutar(estado, new PeticionPagina(pagina)));
    }

    @PutMapping("/api/conflictos-canal/{id}/resolver")
    public ConflictoCanalResponse resolver(@PathVariable UUID id, @Valid @RequestBody ResolverConflictoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return mapper.aConflicto(resolver.ejecutar(new IdConflictoCanal(id), UsuarioAutenticado.de(jwt).correo(),
                request.resolucion()));
    }
}

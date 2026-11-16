package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.FueraDeServicioRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoPorPrepararResponse;
import co.edu.uniquindio.sga.application.dto.response.EstadoOperativoResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentosPorPrepararUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararFueraDeServicioUseCase;
import co.edu.uniquindio.sga.application.usecase.IniciarPreparacionApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.MarcarApartamentoPreparadoUseCase;
import co.edu.uniquindio.sga.application.usecase.VolverAServicioApartamentoUseCase;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import co.edu.uniquindio.sga.infrastructure.web.mapper.OperacionDiariaWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller delgado del estado operativo del apartamento (7.6). Una operación de negocio por
 * transición, nunca un cambio de estado genérico. El personal de servicio prepara; solo el
 * administrador declara FUERA_DE_SERVICIO o lo devuelve a servicio (7.1).
 */
@RestController
public class EstadoOperativoController {

    private static final String SERVICIO = "hasAnyAuthority('PERSONAL_SERVICIO', 'ADMINISTRADOR')";
    private static final String SOLO_ADMINISTRADOR = "hasAuthority('ADMINISTRADOR')";

    private final IniciarPreparacionApartamentoUseCase iniciarPreparacion;
    private final MarcarApartamentoPreparadoUseCase marcarPreparado;
    private final DeclararFueraDeServicioUseCase declararFueraDeServicio;
    private final VolverAServicioApartamentoUseCase volverAServicio;
    private final ConsultarApartamentosPorPrepararUseCase porPreparar;
    private final OperacionDiariaWebMapper mapper;

    public EstadoOperativoController(IniciarPreparacionApartamentoUseCase iniciarPreparacion,
                                     MarcarApartamentoPreparadoUseCase marcarPreparado,
                                     DeclararFueraDeServicioUseCase declararFueraDeServicio,
                                     VolverAServicioApartamentoUseCase volverAServicio,
                                     ConsultarApartamentosPorPrepararUseCase porPreparar,
                                     OperacionDiariaWebMapper mapper) {
        this.iniciarPreparacion = iniciarPreparacion;
        this.marcarPreparado = marcarPreparado;
        this.declararFueraDeServicio = declararFueraDeServicio;
        this.volverAServicio = volverAServicio;
        this.porPreparar = porPreparar;
        this.mapper = mapper;
    }

    /** CU-43: apartamentos PENDIENTE_PREPARACION o EN_PREPARACION. */
    @GetMapping("/api/apartamentos/por-preparar")
    @PreAuthorize("hasAnyAuthority('PERSONAL_SERVICIO', 'RECEPCIONISTA', 'ADMINISTRADOR')")
    public List<ApartamentoPorPrepararResponse> consultarPorPreparar() {
        return porPreparar.ejecutar().stream().map(mapper::aPorPreparar).toList();
    }

    @PutMapping("/api/apartamentos/{identificacion}/iniciar-preparacion")
    @PreAuthorize(SERVICIO)
    public EstadoOperativoResponse iniciarPreparacion(@PathVariable String identificacion,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return mapper.aEstadoOperativo(iniciarPreparacion.ejecutar(new IdentificacionApartamento(identificacion),
                UsuarioAutenticado.de(jwt).correo()));
    }

    @PutMapping("/api/apartamentos/{identificacion}/marcar-preparado")
    @PreAuthorize(SERVICIO)
    public EstadoOperativoResponse marcarPreparado(@PathVariable String identificacion,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return mapper.aEstadoOperativo(marcarPreparado.ejecutar(new IdentificacionApartamento(identificacion),
                UsuarioAutenticado.de(jwt).correo()));
    }

    @PutMapping("/api/apartamentos/{identificacion}/declarar-fuera-de-servicio")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public EstadoOperativoResponse declararFueraDeServicio(@PathVariable String identificacion,
                                                           @Valid @RequestBody FueraDeServicioRequest request,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return mapper.aEstadoOperativo(declararFueraDeServicio.ejecutar(
                new IdentificacionApartamento(identificacion), request.motivo().trim(),
                UsuarioAutenticado.de(jwt).correo()));
    }

    @PutMapping("/api/apartamentos/{identificacion}/volver-a-servicio")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public EstadoOperativoResponse volverAServicio(@PathVariable String identificacion,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return mapper.aEstadoOperativo(volverAServicio.ejecutar(new IdentificacionApartamento(identificacion),
                UsuarioAutenticado.de(jwt).correo()));
    }
}

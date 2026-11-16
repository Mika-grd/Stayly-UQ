package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.AlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.request.ParametrosReglasPropiasRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ConfigurarAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfigurarParametrosReglasPropiasUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.infrastructure.web.mapper.AlojamientoWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller delgado del recurso Alojamiento (F-13, recurso único). Recepción y administración lo
 * consultan; un huésped no accede a la configuración (7.2) y solo el administrador la cambia (7.1, 7.3).
 */
@RestController
public class AlojamientoController {

    private static final String SOLO_ADMINISTRADOR = "hasAuthority('ADMINISTRADOR')";

    private final ConsultarAlojamientoUseCase consultar;
    private final ConfigurarAlojamientoUseCase configurar;
    private final ConfigurarParametrosReglasPropiasUseCase configurarReglasPropias;
    private final AlojamientoWebMapper mapper;

    public AlojamientoController(ConsultarAlojamientoUseCase consultar, ConfigurarAlojamientoUseCase configurar,
                                 ConfigurarParametrosReglasPropiasUseCase configurarReglasPropias,
                                 AlojamientoWebMapper mapper) {
        this.consultar = consultar;
        this.configurar = configurar;
        this.configurarReglasPropias = configurarReglasPropias;
        this.mapper = mapper;
    }

    @GetMapping("/api/alojamiento")
    @PreAuthorize("hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')")
    public AlojamientoResponse consultar() {
        return mapper.aResponse(consultar.ejecutar());
    }

    /** CU-36: reemplaza los datos del alojamiento y los parámetros de la sección 6. */
    @PutMapping("/api/alojamiento")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public AlojamientoResponse configurar(@Valid @RequestBody AlojamientoRequest request) {
        return mapper.aResponse(configurar.ejecutar(mapper.aComando(request)));
    }

    /** CU-37: parámetros de las reglas propias RP-02 y RP-03. */
    @PutMapping("/api/alojamiento/parametros")
    @PreAuthorize(SOLO_ADMINISTRADOR)
    public AlojamientoResponse configurarParametrosReglasPropias(
            @Valid @RequestBody ParametrosReglasPropiasRequest request) {
        return mapper.aResponse(configurarReglasPropias.ejecutar(mapper.aCupoMascotas(request),
                mapper.aCargoPorMascota(request), mapper.aRecargoLlegadaNocturna(request)));
    }
}

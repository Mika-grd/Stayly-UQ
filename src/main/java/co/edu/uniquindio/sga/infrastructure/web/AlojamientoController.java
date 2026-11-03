package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.infrastructure.web.mapper.AlojamientoWebMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller delgado del recurso Alojamiento (F-13, recurso único). Solo consulta: sus valores se
 * cambian en application.properties, sin recompilar (6, 12.4).
 */
@RestController
public class AlojamientoController {

    private final ConsultarAlojamientoUseCase consultar;
    private final AlojamientoWebMapper mapper;

    public AlojamientoController(ConsultarAlojamientoUseCase consultar, AlojamientoWebMapper mapper) {
        this.consultar = consultar;
        this.mapper = mapper;
    }

    @GetMapping("/api/alojamiento")
    public AlojamientoResponse consultar() {
        return mapper.aResponse(consultar.ejecutar());
    }
}

package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.ResumenDiaResponse;
import co.edu.uniquindio.sga.application.usecase.ResumirDiaUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller delgado del asistente de IA (10.2). Siempre responde 200: sin IA, disponible es false. */
@RestController
public class AsistenteController {

    private final ResumirDiaUseCase resumir;

    public AsistenteController(ResumirDiaUseCase resumir) {
        this.resumir = resumir;
    }

    @GetMapping("/api/asistente/resumen-del-dia")
    @PreAuthorize("hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')")
    public ResumenDiaResponse resumenDelDia(@RequestParam(required = false) LocalDate fecha) {
        var resumen = resumir.ejecutar(fecha);
        return new ResumenDiaResponse(resumen.fecha(), resumen.disponible(), resumen.texto().orElse(null),
                resumen.proveedor());
    }
}

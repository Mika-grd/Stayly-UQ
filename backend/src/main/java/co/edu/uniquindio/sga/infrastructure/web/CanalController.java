package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.CanalRequest;
import co.edu.uniquindio.sga.application.dto.response.CanalResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaEventoCanalResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarBitacoraCanalesUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarCanalesUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarCanalUseCase;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.infrastructure.web.mapper.CanalWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Administración de canales externos (7.8): registrar, listar y consultar la bitácora. Solo administrador. */
@RestController
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class CanalController {

    private final RegistrarCanalUseCase registrar;
    private final ConsultarCanalesUseCase listar;
    private final ConsultarBitacoraCanalesUseCase bitacora;
    private final CanalWebMapper mapper;

    public CanalController(RegistrarCanalUseCase registrar, ConsultarCanalesUseCase listar,
                           ConsultarBitacoraCanalesUseCase bitacora, CanalWebMapper mapper) {
        this.registrar = registrar;
        this.listar = listar;
        this.bitacora = bitacora;
        this.mapper = mapper;
    }

    /** La credencial solo viaja en esta respuesta; después se guarda únicamente su huella. */
    @PostMapping("/api/canales")
    public ResponseEntity<CanalResponse> registrar(@Valid @RequestBody CanalRequest request) {
        RegistrarCanalUseCase.CanalRegistrado registrado = registrar.ejecutar(request.nombre(), request.tipo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.aCanal(registrado.canal(), true, registrado.credencial()));
    }

    @GetMapping("/api/canales")
    public List<CanalResponse> listar() {
        return listar.ejecutar().stream()
                .map(canal -> mapper.aCanal(canal.canal(), canal.activo(), null))
                .toList();
    }

    @GetMapping("/api/canales/bitacora")
    public PaginaEventoCanalResponse bitacora(@RequestParam(required = false) String canal,
                                              @RequestParam(required = false) LocalDate desde,
                                              @RequestParam(required = false) LocalDate hasta,
                                              @RequestParam(defaultValue = "0") int pagina) {
        return mapper.aPaginaEventos(bitacora.ejecutar(canal, desde, hasta, new PeticionPagina(pagina)));
    }
}

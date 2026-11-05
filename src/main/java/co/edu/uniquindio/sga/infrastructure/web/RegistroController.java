package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.RegistroHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.ErrorNegocioResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.security.RegistroHuespedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro público de huéspedes (actividad 7.5.2). La ruta es pública en SecurityConfig. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y emisión del JWT")
public class RegistroController {

    private final RegistroHuespedService registroHuesped;

    public RegistroController(RegistroHuespedService registroHuesped) {
        this.registroHuesped = registroHuesped;
    }

    /** POST -> /api/auth/registro */
    @PostMapping("/registro")
    @SecurityRequirements    // endpoint público: Swagger no exige token
    @Operation(summary = "Registrar huésped", description = "Crea un usuario con rol HUESPED")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña insegura",
            content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
    @ApiResponse(responseCode = "409", description = "El correo o el documento ya están registrados",
            content = @Content(schema = @Schema(implementation = ErrorNegocioResponse.class)))
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroHuespedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroHuesped.registrar(request));
    }
}

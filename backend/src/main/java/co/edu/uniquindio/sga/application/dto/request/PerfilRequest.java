package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos del perfil que el propio usuario puede cambiar (schema PerfilRequest, 7.2). El correo no. */
public record PerfilRequest(@NotBlank @Size(max = 100) String nombre, @NotBlank @Size(max = 20) String telefono,
                            @Size(max = 300) String fotoUrl) {
}

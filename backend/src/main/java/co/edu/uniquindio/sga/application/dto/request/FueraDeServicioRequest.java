package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo para declarar el apartamento fuera de servicio (schema FueraDeServicioRequest, CU-15). */
public record FueraDeServicioRequest(@NotBlank @Size(max = 500) String motivo) {
}

package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos de un canal (schema CanalRequest, 7.8). */
public record CanalRequest(@NotBlank @Size(max = 100) String nombre, @NotNull CanalOrigen tipo) {
}

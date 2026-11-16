package co.edu.uniquindio.sga.application.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Servicio adicional del alojamiento (schema ServicioAdicionalDTO); {@code valor} solo si genera cargo. */
public record ServicioAdicionalDTO(@NotBlank String nombre, @NotNull Boolean generaCargo, Long valor) {
}

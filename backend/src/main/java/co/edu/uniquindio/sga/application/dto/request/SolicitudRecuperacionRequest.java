package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Pide un código de recuperación al correo registrado (schema SolicitudRecuperacionRequest, 7.2). */
public record SolicitudRecuperacionRequest(@NotBlank @Email String correo) {
}

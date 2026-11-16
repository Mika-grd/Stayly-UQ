package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;

import java.util.UUID;

/** Canal registrado (schema CanalResponse). {@code credencialGenerada} solo al crearlo; nunca se vuelve a mostrar. */
public record CanalResponse(UUID id, String nombre, CanalOrigen tipo, boolean activo, String credencialGenerada) {
}

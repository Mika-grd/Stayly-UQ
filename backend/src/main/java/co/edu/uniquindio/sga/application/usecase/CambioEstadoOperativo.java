package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;

import java.time.LocalDateTime;

/** Resultado de una transición del estado operativo (7.6): el apartamento, quién la hizo y cuándo. */
public record CambioEstadoOperativo(Apartamento apartamento, String autor, LocalDateTime fecha) {
}

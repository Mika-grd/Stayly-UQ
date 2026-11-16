package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;

/** Apartamento pendiente o en preparación (schema ApartamentoPorPrepararResponse, 7.1, CU-43). */
public record ApartamentoPorPrepararResponse(String identificacion, String nombre, EstadoOperativo estadoOperativo) {
}

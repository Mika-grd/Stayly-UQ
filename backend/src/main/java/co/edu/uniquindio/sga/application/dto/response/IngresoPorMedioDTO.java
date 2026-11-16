package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;

/** Ingresos de un medio de pago (schema IngresoPorMedioDTO). */
public record IngresoPorMedioDTO(MedioPago medio, long total) {
}

package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;

/** Resultado de recibir una reserva externa (schema ReservaExternaResponse, CU-08). */
public record ReservaExternaResponse(String identificadorExterno, String codigoReserva, EstadoReserva estado,
                                     ResultadoReservaExterna resultado, long total) {
}

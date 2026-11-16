package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Disponibilidad para el canal externo (schema DisponibilidadExternaResponse, 10.3.1). */
public record DisponibilidadExternaResponse(LocalDate fechaEntrada, LocalDate fechaSalida,
                                            List<ApartamentoResumenResponse> apartamentos) {
}

package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.application.dto.TramoPoliticaDTO;

import java.time.LocalDateTime;
import java.util.List;

/** Versión de la política de cancelación (schema PoliticaCancelacionResponse). */
public record PoliticaCancelacionResponse(int version, LocalDateTime vigenteDesde, List<TramoPoliticaDTO> tramos,
                                          int porcentajeRetencionNoShow) {
}

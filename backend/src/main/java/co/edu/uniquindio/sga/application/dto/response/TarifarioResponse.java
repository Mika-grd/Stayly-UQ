package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Tarifario vigente e histórico de un apartamento (schema TarifarioResponse). */
public record TarifarioResponse(String identificacionApartamento, List<TarifaVigenteDTO> vigentes,
                                List<TarifaVigenteDTO> historico, boolean completo) {
}

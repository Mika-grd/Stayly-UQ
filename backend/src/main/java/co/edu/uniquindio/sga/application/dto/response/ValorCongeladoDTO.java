package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Valor de la reserva congelado al crear, RN-22 (schema ValorCongeladoDTO). */
public record ValorCongeladoDTO(List<CargoNocheDTO> noches, long total) {
}

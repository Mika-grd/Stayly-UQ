package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.canal.TipoEventoCanal;

import java.time.LocalDateTime;
import java.util.UUID;

/** Evento de la bitácora de canales (schema EventoCanalResponse). */
public record EventoCanalResponse(UUID id, String canal, TipoEventoCanal tipo, String identificadorExterno,
                                  LocalDateTime fecha, String resultado) {
}

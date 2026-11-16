package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;

/** Reservas de un canal (schema ReservasCanalDTO). */
public record ReservasCanalDTO(CanalOrigen canal, int total, int canceladas, int noShow) {
}

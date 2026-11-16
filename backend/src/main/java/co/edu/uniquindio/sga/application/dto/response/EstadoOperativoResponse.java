package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;

import java.time.LocalDateTime;

/** Estado operativo del apartamento tras una transición (schema EstadoOperativoResponse, 7.6). */
public record EstadoOperativoResponse(String identificacion, EstadoOperativo estadoOperativo,
                                      LocalDateTime fechaCambio, String autor) {
}

package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Reserva completa (schema ReservaResponse). */
public record ReservaResponse(String codigo, EstadoReserva estado, CanalOrigen canal, String identificacionApartamento,
                              String advertenciaAcceso, LocalDate fechaEntrada, LocalDate fechaSalida,
                              int numeroNoches, LocalTime horaEstimadaLlegada, List<OcupanteResponse> ocupantes,
                              int mascotasAutorizadas, ValorCongeladoDTO valor, int versionPolitica, long saldoFolio,
                              LocalDateTime fechaCreacion) {
}

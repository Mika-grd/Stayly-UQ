package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Detalle de una reserva (schema ReservaResponse). Los valores son pesos colombianos sin decimales. */
public record ReservaResponse(
        String codigo,
        String estado,
        String canal,
        String identificacionApartamento,
        String advertenciaAcceso,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int numeroNoches,
        LocalTime horaEstimadaLlegada,
        List<OcupanteResponse> ocupantes,
        int mascotasAutorizadas,
        ValorCongeladoResponse valor,
        int versionPolitica,
        long saldoFolio,
        LocalDateTime fechaCreacion
) {

    /** Ocupante de la reserva (schema OcupanteResponse). */
    public record OcupanteResponse(UUID id, String nombre, LocalDate fechaNacimiento, boolean facturable,
                                   boolean titular) {
    }

    /** Valor congelado al crear la reserva (schema ValorCongeladoDTO). */
    public record ValorCongeladoResponse(List<CargoNocheResponse> noches, long total) {
    }

    /** Una noche del valor congelado (schema CargoNocheDTO). */
    public record CargoNocheResponse(LocalDate fecha, String temporada, long tarifa, int ocupantesFacturables,
                                     long subtotal) {
    }
}

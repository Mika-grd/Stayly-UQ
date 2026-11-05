package co.edu.uniquindio.sga.application.dto.response;

/** Liquidación de una cancelación o no-show (schema LiquidacionReservaResponse). */
public record LiquidacionReservaResponse(String codigoReserva, EstadoLiquidacion estado, int versionPolitica,
                                         long retencion, long devolucion, long saldoFolio) {
}

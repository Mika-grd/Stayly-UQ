package co.edu.uniquindio.sga.application.dto.response;

/**
 * Resultado de cancelar una reserva (schema LiquidacionReservaResponse).
 *
 * @param retencion  lo que el alojamiento retiene según la política congelada
 * @param devolucion lo pagado de más, que queda como saldo a favor del huésped
 * @param saldoFolio cargos menos pagos tras la cancelación (negativo = a favor del huésped)
 */
public record LiquidacionReservaResponse(String codigoReserva, String estado, int versionPolitica, long retencion,
                                         long devolucion, long saldoFolio) {
}

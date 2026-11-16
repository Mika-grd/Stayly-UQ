package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Valor de una noche dentro del desglose de una cotización o de una reserva (schema CargoNocheDTO). */
public record CargoNocheDTO(LocalDate fecha, String temporada, long tarifa, int ocupantesFacturables, long subtotal) {
}

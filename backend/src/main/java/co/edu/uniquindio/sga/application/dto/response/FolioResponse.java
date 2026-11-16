package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.EstadoFolio;

import java.util.List;
import java.util.UUID;

/**
 * Folio de la reserva con cargos, pagos y saldo (schema FolioResponse, CU-19). Saldo positivo: debe el
 * huésped; negativo: saldo a favor. {@code autorizacionCierre} solo si se cerró con saldo.
 */
public record FolioResponse(UUID id, String codigoReserva, EstadoFolio estado, List<CargoDTO> cargos,
                            List<PagoDTO> pagos, long totalCargos, long totalPagos, long saldo,
                            AutorizacionCierreDTO autorizacionCierre) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Cotización con desglose noche por noche (schema CotizacionResponse). */
public record CotizacionResponse(String identificacionApartamento, LocalDate fechaEntrada, LocalDate fechaSalida,
                                 int numeroNoches, int ocupantesTotales, int ocupantesFacturables,
                                 List<CargoNocheDTO> noches, long valorAlojamiento, Long cargoMascotas, long total) {
}

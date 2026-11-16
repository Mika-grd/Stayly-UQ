package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Calendario de disponibilidad de un apartamento por rango [desde, hasta) (schema DisponibilidadApartamentoResponse). */
public record DisponibilidadApartamentoResponse(String identificacion, LocalDate desde, LocalDate hasta,
                                                List<NocheDisponibleDTO> noches) {

    /** Una noche y si se puede vender (schema NocheDisponibleDTO). */
    public record NocheDisponibleDTO(LocalDate fecha, boolean disponible) {
    }
}

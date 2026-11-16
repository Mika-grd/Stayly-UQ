package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Calendario de ocupación por rango [desde, hasta) (schema CalendarioOcupacionResponse, CU-35). */
public record CalendarioOcupacionResponse(LocalDate desde, LocalDate hasta, List<OcupacionApartamentoDTO> apartamentos) {

    /** Ocupación de un apartamento noche por noche (schema OcupacionApartamentoDTO). */
    public record OcupacionApartamentoDTO(String identificacion, List<NocheOcupacionDTO> noches) {
    }

    /** Estado de una noche; {@code codigoReserva} solo si está RESERVADA (schema NocheOcupacionDTO). */
    public record NocheOcupacionDTO(LocalDate fecha, EstadoNoche estado, String codigoReserva) {
    }

    /** Estado de una noche en el calendario (schema EstadoNoche). */
    public enum EstadoNoche {
        LIBRE,
        RESERVADA,
        BLOQUEADA
    }
}

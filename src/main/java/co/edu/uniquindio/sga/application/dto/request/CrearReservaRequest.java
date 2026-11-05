package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Cuerpo de POST /api/reservas (schema CrearReservaRequest). El canal no viaja en el cuerpo: PORTAL
 * si la crea un huésped, DIRECTO si la crea recepción (lo decide el controller con el rol del token).
 */
public record CrearReservaRequest(
        @NotBlank(message = "es obligatorio") String identificacionApartamento,
        @NotNull(message = "es obligatoria") LocalDate fechaEntrada,
        @NotNull(message = "es obligatoria") LocalDate fechaSalida,
        LocalTime horaEstimadaLlegada,
        @NotNull(message = "es obligatorio") @Valid TitularRequest titular,
        List<@Valid OcupanteRequest> acompanantes,
        @Min(value = 0, message = "no puede ser negativo") Integer mascotasAutorizadas
) {

    public CrearReservaRequest {
        acompanantes = acompanantes == null ? List.of() : List.copyOf(acompanantes);
        mascotasAutorizadas = mascotasAutorizadas == null ? 0 : mascotasAutorizadas;
    }
}

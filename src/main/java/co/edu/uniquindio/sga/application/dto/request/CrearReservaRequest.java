package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos para crear una reserva (schema CrearReservaRequest, CU-02). El canal no viaja en el
 * cuerpo: lo decide el controller según el actor que invoca.
 */
public record CrearReservaRequest(@NotBlank String identificacionApartamento, @NotNull LocalDate fechaEntrada,
                                  @NotNull LocalDate fechaSalida, LocalTime horaEstimadaLlegada,
                                  @NotNull @Valid TitularRequest titular, @Valid List<OcupanteRequest> acompanantes,
                                  @Min(0) Integer mascotasAutorizadas) {
}

package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Datos modificables de una reserva (schema ModificarReservaRequest, CU-13, RN-14). */
public record ModificarReservaRequest(@NotBlank String identificacionApartamento, @NotNull LocalDate fechaEntrada,
                                      @NotNull LocalDate fechaSalida, @NotNull @Valid TitularRequest titular,
                                      @Valid List<OcupanteRequest> acompanantes, @Min(0) Integer mascotasAutorizadas) {
}

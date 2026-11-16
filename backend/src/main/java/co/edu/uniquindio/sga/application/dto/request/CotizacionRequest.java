package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Datos para cotizar una estancia antes de reservar (schema CotizacionRequest, CU-01). */
public record CotizacionRequest(@NotBlank String identificacionApartamento, @NotNull LocalDate fechaEntrada,
                                @NotNull LocalDate fechaSalida, @NotEmpty List<LocalDate> fechasNacimientoOcupantes,
                                @Min(0) Integer mascotasAutorizadas) {
}

package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Reserva enviada por un canal externo (schema ReservaExternaRequest, CU-08). Canal + identificador es único (RN-19). */
public record ReservaExternaRequest(@NotBlank @Size(max = 100) String identificadorExterno,
                                    @NotBlank String identificacionApartamento, @NotNull LocalDate fechaEntrada,
                                    @NotNull LocalDate fechaSalida, LocalTime horaEstimadaLlegada,
                                    @NotNull @Valid TitularRequest titular, @Valid List<OcupanteRequest> acompanantes,
                                    @Min(0) Integer mascotasAutorizadas) {
}

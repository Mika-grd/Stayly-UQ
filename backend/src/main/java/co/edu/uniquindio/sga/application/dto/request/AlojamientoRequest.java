package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoDTO;
import co.edu.uniquindio.sga.application.dto.response.ServicioAdicionalDTO;
import co.edu.uniquindio.sga.application.dto.response.UbicacionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/** Configuración del alojamiento, recurso único (schema AlojamientoRequest, F-13, CU-36). */
public record AlojamientoRequest(@NotBlank String nombre, @NotBlank String descripcion, @NotBlank String ciudad,
                                 @NotBlank String direccion, @NotNull @Valid UbicacionDTO ubicacion,
                                 @NotNull LocalTime horaEntrada, @NotNull LocalTime horaSalida,
                                 @NotNull List<@NotBlank String> normasConvivencia,
                                 @NotNull List<@Valid ServicioAdicionalDTO> serviciosAdicionales,
                                 @NotNull @Valid ParametrosAlojamientoDTO parametros) {
}

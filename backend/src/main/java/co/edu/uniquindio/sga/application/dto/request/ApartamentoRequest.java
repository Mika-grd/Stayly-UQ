package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.dto.ImagenDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/** Datos descriptivos del apartamento (schema ApartamentoRequest), para crear o actualizar. */
public record ApartamentoRequest(
        @NotBlank String identificacion,
        @NotBlank String nombre,
        @NotNull String descripcion,
        @Min(1) int dormitorios,
        @Min(1) int capacidad,
        Set<String> caracteristicas,
        boolean admiteMascotas,
        String advertenciaAcceso,
        @NotEmpty @Size(min = 1, max = 10) @Valid List<ImagenDTO> imagenes) {
}

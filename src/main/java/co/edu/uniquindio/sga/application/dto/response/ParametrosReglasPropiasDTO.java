package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalTime;

/** Parámetros vigentes de RP-02 (mascotas) y RP-03 (recargo nocturno), dentro de AlojamientoResponse. */
public record ParametrosReglasPropiasDTO(int cupoMascotas, long cargoPorMascotaNoche,
                                         LocalTime horaInicioLlegadaNocturna, long valorRecargoLlegadaNocturna) {
}

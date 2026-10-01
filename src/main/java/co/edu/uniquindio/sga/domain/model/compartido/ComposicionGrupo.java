package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Fechas de nacimiento del grupo, antes de que exista la reserva. Sirve para contar
 * ocupantes (capacidad, 3.2) y facturables (cotización, RN-06).
 */
public record ComposicionGrupo(List<LocalDate> fechasNacimiento) {

    public ComposicionGrupo {
        if (fechasNacimiento == null || fechasNacimiento.isEmpty() || fechasNacimiento.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "El grupo debe tener al menos un ocupante con fecha de nacimiento.");
        }
        fechasNacimiento = List.copyOf(fechasNacimiento);
    }

    public int total() {
        return fechasNacimiento.size();
    }

    /** La edad se mide el día de entrada: quien cumple el umbral durante la estancia no cambia de condición. */
    public int facturablesEn(LocalDate entrada, UmbralEdadFacturable umbral) {
        return (int) fechasNacimiento.stream()
                .filter(nacimiento -> umbral.alcanzadoPor(nacimiento, entrada))
                .count();
    }
}

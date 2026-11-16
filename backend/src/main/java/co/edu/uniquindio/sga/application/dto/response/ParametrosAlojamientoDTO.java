package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;

import java.time.LocalTime;
import java.util.List;

/** Parámetros operativos del alojamiento, sección 6 (schema ParametrosAlojamientoDTO). */
public record ParametrosAlojamientoDTO(int umbralEdadFacturable, int tiempoPreparacionHoras,
                                       int plazoConfirmacionHoras, LocalTime horaLimiteNoShow, boolean exigeAnticipo,
                                       Integer porcentajeAnticipo, List<MedioPago> mediosPagoAceptados) {
}

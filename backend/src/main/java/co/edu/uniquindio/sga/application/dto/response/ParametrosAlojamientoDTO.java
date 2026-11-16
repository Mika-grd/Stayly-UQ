package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/**
 * Parámetros operativos del alojamiento, sección 6 (schema ParametrosAlojamientoDTO). Va en la
 * respuesta y en AlojamientoRequest: las anotaciones solo validan la forma (400); los rangos del
 * negocio (rangos, porcentaje, al menos dos medios de pago) los valida el dominio (409).
 */
public record ParametrosAlojamientoDTO(@NotNull Integer umbralEdadFacturable,
                                       @NotNull Integer tiempoPreparacionHoras,
                                       @NotNull Integer plazoConfirmacionHoras,
                                       @NotNull LocalTime horaLimiteNoShow, @NotNull Boolean exigeAnticipo,
                                       Integer porcentajeAnticipo,
                                       @NotNull List<MedioPago> mediosPagoAceptados) {
}

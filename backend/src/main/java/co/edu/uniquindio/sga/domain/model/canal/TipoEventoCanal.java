package co.edu.uniquindio.sga.domain.model.canal;

/** Qué anota la bitácora del canal (7.8). El evento guarda el nombre; este enum fija los valores válidos. */
public enum TipoEventoCanal {
    DISPONIBILIDAD_CONSULTADA,
    RESERVA_RECIBIDA,
    RESERVA_DUPLICADA,
    CONFLICTO_REGISTRADO,
    CANCELACION_RECIBIDA
}

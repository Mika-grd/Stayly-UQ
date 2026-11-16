package co.edu.uniquindio.sga.domain.model.canal;

/** Qué pasa con una reserva externa: es nueva, ya existía (RN-19) o choca con una vigente (RN-18). */
public enum TipoConciliacion {
    ACEPTAR,
    YA_EXISTE,
    CONFLICTO
}

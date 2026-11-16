package co.edu.uniquindio.sga.domain.model.reserva;

/** Ciclo de vida de la reserva (sección 8, F-06, RN-08). */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA,
    NO_SHOW;

    public boolean puedeTransicionarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == EN_CURSO || destino == CANCELADA || destino == NO_SHOW;
            case EN_CURSO -> destino == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }

    /** Son activas PENDIENTE, CONFIRMADA y EN_CURSO: las demás liberan sus noches (RN-12). */
    public boolean retieneDisponibilidad() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    public boolean esTerminal() {
        return this == FINALIZADA || this == CANCELADA || this == NO_SHOW;
    }
}

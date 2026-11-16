package co.edu.uniquindio.sga.domain.model.apartamento;

/**
 * Estado operativo del apartamento (3.3) con las transiciones de 7.6. FUERA_DE_SERVICIO se
 * declara desde cualquier estado no ocupado y solo sale a PENDIENTE_PREPARACION.
 */
public enum EstadoOperativo {
    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    public boolean puedeTransicionarA(EstadoOperativo destino) {
        return switch (this) {
            case PENDIENTE_PREPARACION -> destino == EN_PREPARACION || destino == FUERA_DE_SERVICIO;
            case EN_PREPARACION -> destino == PREPARADO || destino == FUERA_DE_SERVICIO;
            case PREPARADO -> destino == OCUPADO || destino == FUERA_DE_SERVICIO;
            case OCUPADO -> destino == PENDIENTE_PREPARACION;
            case FUERA_DE_SERVICIO -> destino == PENDIENTE_PREPARACION;
        };
    }

    /** RN-11: solo un apartamento PREPARADO puede recibir un grupo. */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }
}

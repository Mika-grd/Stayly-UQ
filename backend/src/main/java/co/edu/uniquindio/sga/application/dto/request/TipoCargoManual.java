package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;

/** Cargos que se registran a mano (schema TipoCargoManual); ALOJAMIENTO y PENALIDAD los pone el sistema. */
public enum TipoCargoManual {
    SERVICIO_ADICIONAL,
    AJUSTE;

    public TipoCargo aTipoCargo() {
        return TipoCargo.valueOf(name());
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloqueoRepositoryEnMemoria implements BloqueoRepository {

    private final Map<IdBloqueo, Bloqueo> bloqueos = new HashMap<>();

    @Override
    public void guardar(Bloqueo bloqueo) {
        bloqueos.put(bloqueo.id(), bloqueo);
    }

    @Override
    public List<Bloqueo> buscarVigentesQueSolapan(IdentificacionApartamento apartamento, Estancia estancia) {
        return bloqueos.values().stream()
                .filter(bloqueo -> bloqueo.apartamento().equals(apartamento))
                .filter(bloqueo -> bloqueo.afecta(estancia))
                .toList();
    }
}

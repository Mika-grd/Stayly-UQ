package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictoCanalRepositoryEnMemoria implements ConflictoCanalRepository {

    private final Map<IdConflictoCanal, ConflictoCanal> conflictos = new HashMap<>();

    @Override
    public void guardar(ConflictoCanal conflicto) {
        conflictos.put(conflicto.id(), conflicto);
    }

    @Override
    public List<ConflictoCanal> listarPendientes() {
        return conflictos.values().stream()
                .filter(conflicto -> conflicto.estado() == EstadoConflicto.PENDIENTE)
                .toList();
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConflictoCanalRepositoryEnMemoria implements ConflictoCanalRepository {

    private final Map<IdConflictoCanal, ConflictoCanal> conflictos = new HashMap<>();

    @Override
    public void guardar(ConflictoCanal conflicto) {
        conflictos.put(conflicto.id(), conflicto);
    }

    @Override
    public Optional<ConflictoCanal> buscarPorId(IdConflictoCanal id) {
        return Optional.ofNullable(conflictos.get(id));
    }

    @Override
    public List<ConflictoCanal> listarPendientes() {
        return conflictos.values().stream()
                .filter(conflicto -> conflicto.estado() == EstadoConflicto.PENDIENTE)
                .toList();
    }

    @Override
    public Pagina<ConflictoCanal> buscar(EstadoConflicto estado, PeticionPagina pagina) {
        return Pagina.de(conflictos.values().stream()
                .filter(conflicto -> estado == null || conflicto.estado() == estado)
                .sorted(Comparator.comparing(ConflictoCanal::fechaRegistro))
                .toList(), pagina);
    }
}

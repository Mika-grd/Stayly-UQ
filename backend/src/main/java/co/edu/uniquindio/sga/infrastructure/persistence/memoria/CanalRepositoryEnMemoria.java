package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CanalRepositoryEnMemoria implements CanalRepository {

    private final Map<IdCanal, Canal> canales = new HashMap<>();

    @Override
    public void guardar(Canal canal) {
        canales.put(canal.id(), canal);
    }

    @Override
    public Optional<Canal> buscarPorId(IdCanal id) {
        return Optional.ofNullable(canales.get(id));
    }

    @Override
    public List<Canal> listar() {
        return canales.values().stream().sorted(Comparator.comparing(Canal::nombre)).toList();
    }
}

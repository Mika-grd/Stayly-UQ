package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;

import java.util.Optional;

public interface CanalRepository {

    void guardar(Canal canal);

    Optional<Canal> buscarPorId(IdCanal id);
}

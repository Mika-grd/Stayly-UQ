package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CanalMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CanalJpaRepository;

import java.util.Optional;

/** Adaptador JPA del puerto CanalRepository. */
public class CanalRepositoryJpa implements CanalRepository {

    private final CanalJpaRepository jpa;

    public CanalRepositoryJpa(CanalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Canal canal) {
        jpa.save(CanalMapper.aEntidad(canal));
    }

    @Override
    public Optional<Canal> buscarPorId(IdCanal id) {
        return jpa.findById(id.valor()).map(CanalMapper::aDominio);
    }
}

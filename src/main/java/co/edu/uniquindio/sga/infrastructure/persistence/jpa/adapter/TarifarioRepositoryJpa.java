package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TarifarioMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TarifarioJpaRepository;

import java.util.Optional;

/** Adaptador JPA del puerto TarifarioRepository. */
public class TarifarioRepositoryJpa implements TarifarioRepository {

    private final TarifarioJpaRepository jpa;

    public TarifarioRepositoryJpa(TarifarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Tarifario tarifario) {
        jpa.save(TarifarioMapper.aEntidad(tarifario));
    }

    @Override
    public Optional<Tarifario> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return jpa.findById(apartamento.valor()).map(TarifarioMapper::aDominio);
    }
}

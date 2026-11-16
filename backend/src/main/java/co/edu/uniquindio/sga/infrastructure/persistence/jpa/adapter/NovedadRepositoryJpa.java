package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.NovedadMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.NovedadJpaRepository;

import java.util.List;

/** Adaptador JPA del puerto NovedadRepository. */
public class NovedadRepositoryJpa implements NovedadRepository {

    private final NovedadJpaRepository jpa;

    public NovedadRepositoryJpa(NovedadJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Novedad novedad) {
        jpa.save(NovedadMapper.aEntidad(novedad));
    }

    @Override
    public List<Novedad> listarPorApartamento(IdentificacionApartamento apartamento) {
        return jpa.findByApartamentoOrderByFechaAsc(apartamento.valor()).stream()
                .map(NovedadMapper::aDominio)
                .toList();
    }
}

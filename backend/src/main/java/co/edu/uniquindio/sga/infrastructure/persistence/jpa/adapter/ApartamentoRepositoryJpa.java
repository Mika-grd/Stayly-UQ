package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ApartamentoMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ApartamentoRepository. */
public class ApartamentoRepositoryJpa implements ApartamentoRepository {

    private final ApartamentoJpaRepository jpa;

    public ApartamentoRepositoryJpa(ApartamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Apartamento apartamento) {
        jpa.save(ApartamentoMapper.aEntidad(apartamento));
    }

    @Override
    public Optional<Apartamento> buscarPorIdentificacion(IdentificacionApartamento identificacion) {
        return jpa.findById(identificacion.valor()).map(ApartamentoMapper::aDominio);
    }

    @Override
    public List<Apartamento> listarActivos() {
        return jpa.findByActivoTrue().stream().map(ApartamentoMapper::aDominio).toList();
    }
}

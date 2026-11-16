package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.BloqueoMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.BloqueoJpaRepository;

import java.util.List;

/** Adaptador JPA del puerto BloqueoRepository. */
public class BloqueoRepositoryJpa implements BloqueoRepository {

    private final BloqueoJpaRepository jpa;

    public BloqueoRepositoryJpa(BloqueoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        jpa.save(BloqueoMapper.aEntidad(bloqueo));
    }

    @Override
    public List<Bloqueo> buscarVigentesQueSolapan(IdentificacionApartamento apartamento, Estancia estancia) {
        return jpa.buscarVigentesQueSolapan(apartamento.valor(), estancia.entrada(), estancia.salida()).stream()
                .map(BloqueoMapper::aDominio)
                .toList();
    }
}

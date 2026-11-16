package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public Pagina<Novedad> buscarPorApartamento(IdentificacionApartamento apartamento, PeticionPagina pagina) {
        Page<NovedadEntity> resultado = jpa.findByApartamento(apartamento.valor(),
                PageRequest.of(pagina.numero(), pagina.tamano(), Sort.by(Sort.Direction.DESC, "fecha")));
        return new Pagina<>(resultado.getContent().stream().map(NovedadMapper::aDominio).toList(), pagina.numero(),
                pagina.tamano(), resultado.getTotalElements());
    }
}

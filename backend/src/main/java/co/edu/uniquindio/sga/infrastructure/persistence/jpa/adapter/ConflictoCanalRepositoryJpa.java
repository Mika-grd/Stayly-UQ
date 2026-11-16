package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConflictoCanalEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ConflictoCanalMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConflictoCanalJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ConflictoCanalRepository. */
public class ConflictoCanalRepositoryJpa implements ConflictoCanalRepository {

    private final ConflictoCanalJpaRepository jpa;

    public ConflictoCanalRepositoryJpa(ConflictoCanalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(ConflictoCanal conflicto) {
        jpa.save(ConflictoCanalMapper.aEntidad(conflicto));
    }

    @Override
    public Optional<ConflictoCanal> buscarPorId(IdConflictoCanal id) {
        return jpa.findById(id.valor()).map(ConflictoCanalMapper::aDominio);
    }

    @Override
    public List<ConflictoCanal> listarPendientes() {
        return jpa.findByEstadoOrderByFechaRegistroAsc(EstadoConflicto.PENDIENTE).stream()
                .map(ConflictoCanalMapper::aDominio)
                .toList();
    }

    @Override
    public Pagina<ConflictoCanal> buscar(EstadoConflicto estado, PeticionPagina pagina) {
        PageRequest peticion = PageRequest.of(pagina.numero(), pagina.tamano(), Sort.by("fechaRegistro").ascending());
        Page<ConflictoCanalEntity> resultado = estado == null ? jpa.findAll(peticion) : jpa.findByEstado(estado, peticion);
        return new Pagina<>(resultado.getContent().stream().map(ConflictoCanalMapper::aDominio).toList(),
                pagina.numero(), pagina.tamano(), resultado.getTotalElements());
    }
}

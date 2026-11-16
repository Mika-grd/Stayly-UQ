package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.FolioMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.FolioJpaRepository;

import java.util.Optional;

/** Adaptador JPA del puerto FolioRepository. */
public class FolioRepositoryJpa implements FolioRepository {

    private final FolioJpaRepository jpa;

    public FolioRepositoryJpa(FolioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Folio folio) {
        jpa.save(FolioMapper.aEntidad(folio));
    }

    @Override
    public Optional<Folio> buscarPorCodigoReserva(CodigoReserva codigo) {
        return jpa.findByReserva(codigo.valor()).map(FolioMapper::aDominio);
    }
}

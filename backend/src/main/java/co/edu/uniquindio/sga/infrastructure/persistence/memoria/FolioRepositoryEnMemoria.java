package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FolioRepositoryEnMemoria implements FolioRepository {

    private final Map<CodigoReserva, Folio> folios = new HashMap<>();

    @Override
    public void guardar(Folio folio) {
        folios.put(folio.reserva(), folio);
    }

    @Override
    public Optional<Folio> buscarPorCodigoReserva(CodigoReserva codigo) {
        return Optional.ofNullable(folios.get(codigo));
    }
}

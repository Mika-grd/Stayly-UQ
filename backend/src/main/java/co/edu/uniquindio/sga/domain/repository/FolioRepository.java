package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;

import java.util.Optional;

public interface FolioRepository {

    void guardar(Folio folio);

    Optional<Folio> buscarPorCodigoReserva(CodigoReserva codigo);
}

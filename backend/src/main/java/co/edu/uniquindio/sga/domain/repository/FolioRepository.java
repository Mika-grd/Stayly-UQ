package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FolioRepository {

    void guardar(Folio folio);

    Optional<Folio> buscarPorCodigoReserva(CodigoReserva codigo);

    /** Folios con al menos un pago fechado en [desde, hasta) (reporte de ingresos). */
    List<Folio> buscarConPagosEntre(LocalDateTime desde, LocalDateTime hasta);
}

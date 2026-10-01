package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

/** Cargas comunes de los casos de uso, con el error *_NO_ENCONTRADA de la hoja Errores de Negocio. */
final class Consultas {

    private Consultas() {
    }

    static Reserva reserva(ReservaRepository reservas, CodigoReserva codigo) {
        return reservas.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaDominioException("RESERVA_NO_ENCONTRADA", "No existe la reserva " + codigo + "."));
    }

    static Folio folio(FolioRepository folios, CodigoReserva codigo) {
        return folios.buscarPorCodigoReserva(codigo)
                .orElseThrow(() -> new ReglaDominioException("FOLIO_NO_ENCONTRADO",
                        "No existe el folio de la reserva " + codigo + "."));
    }
}

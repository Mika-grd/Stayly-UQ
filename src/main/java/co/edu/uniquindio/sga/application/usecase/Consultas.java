package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

/** Cargas comunes de los casos de uso, con el error *_NO_ENCONTRADO de la hoja Errores de Negocio. */
final class Consultas {

    private Consultas() {
    }

    static Reserva reserva(ReservaRepository reservas, CodigoReserva codigo) {
        return reservas.buscarPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("RESERVA_NO_ENCONTRADA",
                        "No existe la reserva " + codigo + "."));
    }

    static Folio folio(FolioRepository folios, CodigoReserva codigo) {
        return folios.buscarPorCodigoReserva(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("FOLIO_NO_ENCONTRADO",
                        "No existe el folio de la reserva " + codigo + "."));
    }

    static Apartamento apartamento(ApartamentoRepository apartamentos, IdentificacionApartamento identificacion) {
        return apartamentos.buscarPorIdentificacion(identificacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("APARTAMENTO_NO_ENCONTRADO",
                        "No existe el apartamento " + identificacion + "."));
    }

    static Temporada temporada(TemporadaRepository temporadas, IdTemporada id) {
        return temporadas.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("TEMPORADA_NO_ENCONTRADA",
                        "No existe la temporada " + id.valor() + "."));
    }
}

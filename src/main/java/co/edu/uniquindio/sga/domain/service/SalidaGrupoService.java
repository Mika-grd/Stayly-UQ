package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;

import java.time.LocalDateTime;

/**
 * 7.6: el folio cerrado es requisito de la salida, pero el Folio es otro agregado. Verifica que
 * los tres correspondan y que ambos cambios sean válidos antes de aplicar cualquiera.
 */
public class SalidaGrupoService {

    public void registrarSalida(Reserva reserva, Folio folio, Apartamento apartamento, String autor,
                                LocalDateTime ahora) {
        if (!folio.reserva().equals(reserva.codigo()) || !reserva.apartamento().equals(apartamento.identificacion())) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La reserva, el folio y el apartamento no se corresponden.");
        }
        if (!folio.estaCerrado()) {
            throw new ReglaDominioException("FOLIO_ABIERTO", "Debe cerrar el folio antes de registrar la salida.");
        }
        if (!reserva.estado().puedeTransicionarA(EstadoReserva.FINALIZADA)) {
            throw new ReglaDominioException("TRANSICION_INVALIDA",
                    "La reserva en estado " + reserva.estado() + " no puede pasar a FINALIZADA.");
        }
        if (!apartamento.estadoOperativo().puedeTransicionarA(EstadoOperativo.PENDIENTE_PREPARACION)) {
            throw new ReglaDominioException("TRANSICION_ESTADO_OPERATIVO_INVALIDA",
                    "El apartamento no puede pasar de " + apartamento.estadoOperativo() + " a PENDIENTE_PREPARACION.");
        }
        reserva.registrarSalida(autor, ahora);
        apartamento.liberar();
    }
}

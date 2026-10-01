package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;

import java.time.LocalDateTime;

/**
 * RN-10 y RN-11: registrar la llegada cambia dos agregados. Valida la reserva y el apartamento
 * antes de cambiar cualquiera, para que nunca quede uno cambiado y el otro no.
 */
public class EntregaApartamentoService {

    public void registrarLlegada(Reserva reserva, Apartamento apartamento, String autor, LocalDateTime ahora) {
        if (!reserva.apartamento().equals(apartamento.identificacion())) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La reserva " + reserva.codigo() + " no es del apartamento " + apartamento.identificacion() + ".");
        }
        if (!reserva.puedeRegistrarLlegada(ahora.toLocalDate())) {
            if (reserva.estado() != EstadoReserva.CONFIRMADA) {
                throw new ReglaDominioException("TRANSICION_INVALIDA",
                        "La reserva en estado " + reserva.estado() + " no puede pasar a EN_CURSO.");
            }
            throw new ReglaDominioException("REGISTRO_ANTES_DE_ENTRADA",
                    "No se puede registrar la llegada antes de la fecha de entrada.");
        }
        if (!apartamento.puedeRecibirGrupo()) {
            throw new ReglaDominioException("APARTAMENTO_NO_PREPARADO",
                    "El apartamento no está preparado para recibir al grupo.");
        }
        reserva.registrarLlegada(autor, ahora);
        apartamento.marcarOcupado();
    }
}

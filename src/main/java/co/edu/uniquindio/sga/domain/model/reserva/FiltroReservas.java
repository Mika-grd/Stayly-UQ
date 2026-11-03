package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;

import java.time.LocalDate;

/**
 * Filtros del listado de reservas (7.5): todos opcionales. {@code titular} busca en el nombre, el
 * documento o el correo del titular. {@code desde} y {@code hasta} traen las reservas cuya estancia
 * comparte al menos una noche con [desde, hasta).
 */
public record FiltroReservas(EstadoReserva estado, CanalOrigen canal, IdentificacionApartamento apartamento,
                             String titular, LocalDate desde, LocalDate hasta) {

    public FiltroReservas {
        if (desde != null && hasta != null && !hasta.isAfter(desde)) {
            throw new ReglaDominioException("RANGO_FECHAS_INVALIDO",
                    "La fecha final debe ser posterior a la fecha inicial.");
        }
        titular = titular == null || titular.isBlank() ? null : titular.trim();
    }

    public static FiltroReservas ninguno() {
        return new FiltroReservas(null, null, null, null, null, null);
    }

    /** Si la reserva cumple todos los filtros indicados. */
    public boolean admite(Reserva reserva) {
        return (estado == null || reserva.estado() == estado)
                && (canal == null || reserva.canal() == canal)
                && (apartamento == null || reserva.apartamento().equals(apartamento))
                && (desde == null || reserva.estancia().salida().isAfter(desde))
                && (hasta == null || reserva.estancia().entrada().isBefore(hasta))
                && (titular == null || coincideTitular(reserva.titular()));
    }

    private boolean coincideTitular(Ocupante titularReserva) {
        String buscado = titular.toLowerCase();
        return titularReserva.nombre().toLowerCase().contains(buscado)
                || titularReserva.documento().map(doc -> doc.numero().toLowerCase().contains(buscado)).orElse(false)
                || titularReserva.contacto().map(c -> c.correo().toLowerCase().contains(buscado)).orElse(false);
    }
}

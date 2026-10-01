package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;

import java.time.Clock;
import java.time.LocalDateTime;

/** CU-03: deja la reserva en firme (RN-08, RN-09, L-11). */
public class ConfirmarReservaUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ConfirmacionReservaService confirmacion;
    private final Clock reloj;

    public ConfirmarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                   ConfirmacionReservaService confirmacion, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.confirmacion = confirmacion;
        this.reloj = reloj;
    }

    public Reserva ejecutar(CodigoReserva codigo, String autor) {
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        confirmacion.confirmar(reserva, folio, autor, LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        return reserva;
    }
}

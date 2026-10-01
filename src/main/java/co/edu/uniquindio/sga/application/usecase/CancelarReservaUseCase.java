package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-04: cancela una reserva PENDIENTE o CONFIRMADA (RN-08, RN-12, RN-13). La retención sale de
 * la política congelada y el folio anula el alojamiento y registra la penalidad.
 */
public class CancelarReservaUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final CalculadorRetencionService calculadorRetencion;
    private final Clock reloj;

    public CancelarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                  CalculadorRetencionService calculadorRetencion, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.calculadorRetencion = calculadorRetencion;
        this.reloj = reloj;
    }

    public Folio ejecutar(CodigoReserva codigo, String autor) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        Dinero retencion = calculadorRetencion.calcularRetencion(reserva, ahora);
        reserva.cancelar(autor, ahora);
        folio.liquidarCancelacion(retencion, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
        return folio;
    }
}

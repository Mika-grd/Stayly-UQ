package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-12: desde la hora límite del día de entrada (L-15), Reserva.declararNoShow pasa la reserva
 * CONFIRMADA a NO_SHOW y libera sus noches (RN-12). CalculadorRetencionService calcula la
 * consecuencia con la política congelada (RN-13) y el folio la registra como penalidad.
 */
@Service
@Transactional
public class DeclararNoShowUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final CalculadorRetencionService calculadorRetencion;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public DeclararNoShowUseCase(ReservaRepository reservas, FolioRepository folios,
                                 CalculadorRetencionService calculadorRetencion, ConfiguracionAlojamiento configuracion,
                                 Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.calculadorRetencion = calculadorRetencion;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public Folio ejecutar(CodigoReserva codigo, String autor) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        reserva.declararNoShow(configuracion.horaLimiteNoShow(), autor, ahora);
        Dinero penalidad = calculadorRetencion.calcularPenalidadNoShow(reserva);
        folio.liquidarCancelacion(penalidad, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
        return folio;
    }
}

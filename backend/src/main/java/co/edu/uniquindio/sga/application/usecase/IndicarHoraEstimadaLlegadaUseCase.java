package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-14 (RN-09, RP-03): registra o cambia la hora estimada de llegada, solo antes del registro.
 * RecargoLlegadaNocturna decide si corresponde el recargo, su movimiento inverso o nada según la
 * hora anterior y la nueva; el caso de uso solo lo registra en el folio.
 */
@Service
@Transactional
public class IndicarHoraEstimadaLlegadaUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public IndicarHoraEstimadaLlegadaUseCase(ReservaRepository reservas, FolioRepository folios,
                                             ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public HoraIndicada ejecutar(CodigoReserva codigo, HoraEstimadaLlegada hora, String autor) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        HoraEstimadaLlegada anterior = reserva.horaEstimadaLlegada().orElse(null);
        reserva.indicarHoraEstimadaLlegada(hora, autor, ahora);
        configuracion.recargoLlegadaNocturna().cargoPorCambioDeHora(anterior, hora, ahora)
                .ifPresent(folio::registrarCargo);
        reservas.guardar(reserva);
        folios.guardar(folio);
        return new HoraIndicada(reserva, folio);
    }

    public record HoraIndicada(Reserva reserva, Folio folio) {
    }
}

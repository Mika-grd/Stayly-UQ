package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.folio.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CU-18 (7.7): con saldo cero el folio se cierra sin más. Con saldo distinto de cero solo se cierra
 * con la autorización del administrador y su motivo (F-3, RN-17); quién es administrador lo decide el
 * controller, porque el dominio no conoce usuarios.
 */
@Service
@Transactional
public class CerrarFolioUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final Clock reloj;

    public CerrarFolioUseCase(ReservaRepository reservas, FolioRepository folios, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.reloj = reloj;
    }

    /** @param motivoAdministrador motivo de la autorización, solo si quien cierra es administrador. */
    public Folio ejecutar(CodigoReserva codigo, String autor, Optional<String> motivoAdministrador) {
        Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (folio.saldo().esCero()) {
            folio.cerrar(ahora);
        } else {
            folio.cerrarConAutorizacion(motivoAdministrador.filter(motivo -> !motivo.isBlank())
                    .map(motivo -> new AutorizacionCierre(autor, motivo.trim(), ahora))
                    .orElse(null));
        }
        folios.guardar(folio);
        return folio;
    }
}

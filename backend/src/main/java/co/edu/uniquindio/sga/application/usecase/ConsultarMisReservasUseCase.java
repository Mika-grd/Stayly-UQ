package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las reservas del huésped autenticado (portal, 7.2): las que tienen como titular su documento,
 * de la más reciente a la más antigua, de a 10. El documento sale del token, no de la petición.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarMisReservasUseCase {

    private final ReservaRepository reservas;

    public ConsultarMisReservasUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public Pagina<Reserva> ejecutar(String documentoTitular, PeticionPagina pagina) {
        return reservas.buscarPorDocumentoTitular(documentoTitular, pagina);
    }
}

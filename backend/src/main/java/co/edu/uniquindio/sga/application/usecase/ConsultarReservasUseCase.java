package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.5: reservas filtradas por fechas, estado, apartamento, canal y titular, de la más reciente a la más antigua, de a 10. */
@Service
@Transactional(readOnly = true)
public class ConsultarReservasUseCase {

    private final ReservaRepository reservas;

    public ConsultarReservasUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public Pagina<Reserva> ejecutar(FiltroReservas filtro, PeticionPagina pagina) {
        return reservas.buscar(filtro, pagina);
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 7.1: una reserva con su estado e historia. La verificación por propietario (el huésped solo ve
 * las suyas) queda para la seguridad con JWT.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarReservaUseCase {

    private final ReservaRepository reservas;

    public ConsultarReservaUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public Reserva ejecutar(CodigoReserva codigo) {
        return Consultas.reserva(reservas, codigo);
    }
}

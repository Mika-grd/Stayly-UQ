package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** CU-05: entrega el apartamento al grupo (RN-10, RN-11). */
@Service
@Transactional
public class RegistrarLlegadaUseCase {

    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final EntregaApartamentoService entrega;
    private final Clock reloj;

    public RegistrarLlegadaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
                                   EntregaApartamentoService entrega, Clock reloj) {
        this.reservas = reservas;
        this.apartamentos = apartamentos;
        this.entrega = entrega;
        this.reloj = reloj;
    }

    public Reserva ejecutar(CodigoReserva codigo, String autor) {
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Apartamento apartamento = Consultas.apartamento(apartamentos, reserva.apartamento());
        entrega.registrarLlegada(reserva, apartamento, autor, LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
        return reserva;
    }
}

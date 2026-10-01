package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;

import java.time.Clock;
import java.time.LocalDateTime;

/** CU-05: entrega el apartamento al grupo (RN-10, RN-11). */
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
        Apartamento apartamento = apartamentos.buscarPorIdentificacion(reserva.apartamento())
                .orElseThrow(() -> new ReglaDominioException("APARTAMENTO_NO_ENCONTRADO",
                        "No existe el apartamento " + reserva.apartamento() + "."));
        entrega.registrarLlegada(reserva, apartamento, autor, LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
        return reserva;
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.SalidaGrupoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-06 (7.6): SalidaGrupoService exige el folio cerrado; la reserva pasa a FINALIZADA y el apartamento
 * queda PENDIENTE_PREPARACION. La salida anticipada también pasa por aquí (7.5).
 */
@Service
@Transactional
public class RegistrarSalidaUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ApartamentoRepository apartamentos;
    private final SalidaGrupoService salida;
    private final Clock reloj;

    public RegistrarSalidaUseCase(ReservaRepository reservas, FolioRepository folios,
                                  ApartamentoRepository apartamentos, SalidaGrupoService salida, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.apartamentos = apartamentos;
        this.salida = salida;
        this.reloj = reloj;
    }

    public Reserva ejecutar(CodigoReserva codigo, String autor) {
        Reserva reserva = Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        Apartamento apartamento = Consultas.apartamento(apartamentos, reserva.apartamento());
        salida.registrarSalida(reserva, folio, apartamento, autor, LocalDateTime.now(reloj));
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
        return reserva;
    }
}

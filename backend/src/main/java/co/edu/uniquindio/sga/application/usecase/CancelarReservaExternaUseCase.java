package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.TipoEventoCanal;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 7.8: el canal cancela una reserva que él envió, con la liquidación de la política congelada (CU-04).
 * Solo ve las suyas: el identificador externo se busca junto con el canal autenticado (RN-19).
 */
@Service
@Transactional
public class CancelarReservaExternaUseCase {

    private final CanalRepository canales;
    private final ReservaRepository reservas;
    private final CancelarReservaUseCase cancelar;
    private final Clock reloj;

    public CancelarReservaExternaUseCase(CanalRepository canales, ReservaRepository reservas,
                                         CancelarReservaUseCase cancelar, Clock reloj) {
        this.canales = canales;
        this.reservas = reservas;
        this.cancelar = cancelar;
        this.reloj = reloj;
    }

    public Cancelacion ejecutar(IdCanal idCanal, String identificadorExterno) {
        Canal canal = Consultas.canal(canales, idCanal);
        IdentificadorExterno identificador = new IdentificadorExterno(canal.id(), identificadorExterno);
        Reserva reserva = reservas.buscarPorIdentificadorExterno(identificador)
                .orElseThrow(() -> new RecursoNoEncontradoException("RESERVA_NO_ENCONTRADA",
                        "El canal no tiene una reserva con el identificador " + identificador.codigo() + "."));
        Folio folio = cancelar.ejecutar(reserva.codigo(), canal.nombre());
        canal.registrarEvento(new EventoCanal(TipoEventoCanal.CANCELACION_RECIBIDA.name(), identificador.codigo(),
                LocalDateTime.now(reloj), "CANCELADA " + reserva.codigo().valor()));
        canales.guardar(canal);
        return new Cancelacion(Consultas.reserva(reservas, reserva.codigo()), folio);
    }

    public record Cancelacion(Reserva reserva, Folio folio) {
    }
}

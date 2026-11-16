package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RN-18 y RN-19: decide si una reserva externa es nueva, repetida o en conflicto. Nunca toca la
 * reserva vigente; el caso de uso guarda la reserva nueva o el conflicto.
 */
public class ConciliacionCanalExternoService {

    private final ReservaRepository reservas;
    private final DisponibilidadApartamentoService disponibilidad;

    public ConciliacionCanalExternoService(ReservaRepository reservas, DisponibilidadApartamentoService disponibilidad) {
        this.reservas = reservas;
        this.disponibilidad = disponibilidad;
    }

    /**
     * YA_EXISTE si el mismo canal ya envió ese identificador; CONFLICTO si choca con una reserva
     * vigente; en otro caso valida el resto de la disponibilidad (capacidad, bloqueos,
     * preparación) y responde ACEPTAR.
     */
    public ResultadoConciliacion conciliar(IdentificadorExterno identificador, Apartamento apartamento,
                                           Estancia estancia, int totalOcupantes, LocalDateTime ahora) {
        Optional<Reserva> existente = reservas.buscarPorIdentificadorExterno(identificador);
        if (existente.isPresent()) {
            return ResultadoConciliacion.yaExiste(existente.get().codigo());
        }
        List<Reserva> vigentes = reservas.buscarActivasQueSolapan(apartamento.identificacion(), estancia);
        if (!vigentes.isEmpty()) {
            ConflictoCanal conflicto = ConflictoCanal.registrar(new IdConflictoCanal(UUID.randomUUID()), identificador,
                    apartamento.identificacion(), estancia, vigentes.get(0).codigo(), ahora);
            return ResultadoConciliacion.conflicto(conflicto);
        }
        disponibilidad.verificarDisponible(apartamento, estancia, totalOcupantes, null);
        return ResultadoConciliacion.aceptar();
    }
}

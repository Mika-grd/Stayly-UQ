package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-21 (7.6): Apartamento.volverAServicio solo acepta FUERA_DE_SERVICIO → PENDIENTE_PREPARACION, para
 * que el personal lo revise antes de volver a venderlo. El bloqueo, si lo hay, se levanta aparte.
 */
@Service
@Transactional
public class VolverAServicioApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final Clock reloj;

    public VolverAServicioApartamentoUseCase(ApartamentoRepository apartamentos, Clock reloj) {
        this.apartamentos = apartamentos;
        this.reloj = reloj;
    }

    public CambioEstadoOperativo ejecutar(IdentificacionApartamento identificacion, String autor) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        apartamento.volverAServicio();
        apartamentos.guardar(apartamento);
        return new CambioEstadoOperativo(apartamento, autor, LocalDateTime.now(reloj));
    }
}

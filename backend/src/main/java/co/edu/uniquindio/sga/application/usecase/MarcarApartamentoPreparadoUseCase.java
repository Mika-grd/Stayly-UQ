package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-10 (7.6): Apartamento.marcarPreparado solo acepta EN_PREPARACION → PREPARADO. Desde aquí el
 * apartamento puede recibir un grupo (RN-11).
 */
@Service
@Transactional
public class MarcarApartamentoPreparadoUseCase {

    private final ApartamentoRepository apartamentos;
    private final Clock reloj;

    public MarcarApartamentoPreparadoUseCase(ApartamentoRepository apartamentos, Clock reloj) {
        this.apartamentos = apartamentos;
        this.reloj = reloj;
    }

    public CambioEstadoOperativo ejecutar(IdentificacionApartamento identificacion, String autor) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        apartamento.marcarPreparado();
        apartamentos.guardar(apartamento);
        return new CambioEstadoOperativo(apartamento, autor, LocalDateTime.now(reloj));
    }
}

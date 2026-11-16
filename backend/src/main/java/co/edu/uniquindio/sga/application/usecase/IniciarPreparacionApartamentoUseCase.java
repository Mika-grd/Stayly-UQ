package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** CU-09 (7.6): Apartamento.iniciarPreparacion solo acepta PENDIENTE_PREPARACION → EN_PREPARACION. */
@Service
@Transactional
public class IniciarPreparacionApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final Clock reloj;

    public IniciarPreparacionApartamentoUseCase(ApartamentoRepository apartamentos, Clock reloj) {
        this.apartamentos = apartamentos;
        this.reloj = reloj;
    }

    public CambioEstadoOperativo ejecutar(IdentificacionApartamento identificacion, String autor) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        apartamento.iniciarPreparacion();
        apartamentos.guardar(apartamento);
        return new CambioEstadoOperativo(apartamento, autor, LocalDateTime.now(reloj));
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.4: solo se activa un apartamento con tarifa en todas las temporadas (VerificadorTarifasCompletasService). */
@Service
@Transactional
public class ActivarApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final VerificadorTarifasCompletasService verificadorTarifas;

    public ActivarApartamentoUseCase(ApartamentoRepository apartamentos,
                                     VerificadorTarifasCompletasService verificadorTarifas) {
        this.apartamentos = apartamentos;
        this.verificadorTarifas = verificadorTarifas;
    }

    public Apartamento ejecutar(IdentificacionApartamento identificacion) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        verificadorTarifas.verificarTarifasCompletas(identificacion);
        apartamento.activar();
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}

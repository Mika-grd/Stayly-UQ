package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.4: la versión vigente de la política de cancelación (la de mayor número). */
@Service
@Transactional(readOnly = true)
public class ConsultarPoliticaVigenteUseCase {

    private final PoliticaCancelacionRepository politicas;

    public ConsultarPoliticaVigenteUseCase(PoliticaCancelacionRepository politicas) {
        this.politicas = politicas;
    }

    public PoliticaCancelacion ejecutar() {
        return politicas.buscarVigente();
    }
}

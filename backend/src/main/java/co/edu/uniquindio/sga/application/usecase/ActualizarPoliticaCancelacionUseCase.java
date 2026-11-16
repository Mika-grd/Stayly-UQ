package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 7.4 y F-07: actualizar la política crea una versión nueva y conserva la anterior. Las reservas
 * ya creadas siguen con la versión que congelaron (RN-13). PoliticaCancelacion valida P-1.
 */
@Service
@Transactional
public class ActualizarPoliticaCancelacionUseCase {

    private final PoliticaCancelacionRepository politicas;
    private final Clock reloj;

    public ActualizarPoliticaCancelacionUseCase(PoliticaCancelacionRepository politicas, Clock reloj) {
        this.politicas = politicas;
        this.reloj = reloj;
    }

    public PoliticaCancelacion ejecutar(List<TramoPolitica> tramos, int porcentajeRetencionNoShow) {
        PoliticaCancelacion nueva = politicas.buscarVigente()
                .nuevaVersion(tramos, porcentajeRetencionNoShow, LocalDateTime.now(reloj));
        politicas.guardar(nueva);
        return nueva;
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.security.CredencialesCanalService;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 7.8: los canales registrados, sin su credencial; activo si tiene una credencial vigente. */
@Service
@Transactional(readOnly = true)
public class ConsultarCanalesUseCase {

    private final CanalRepository canales;
    private final CredencialesCanalService credenciales;

    public ConsultarCanalesUseCase(CanalRepository canales, CredencialesCanalService credenciales) {
        this.canales = canales;
        this.credenciales = credenciales;
    }

    public List<CanalConEstado> ejecutar() {
        return canales.listar().stream()
                .map(canal -> new CanalConEstado(canal, credenciales.estaActiva(canal.id())))
                .toList();
    }

    public record CanalConEstado(Canal canal, boolean activo) {
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.security.CredencialesCanalService;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 7.8: registra un canal y emite su credencial, que solo se muestra en esta respuesta. */
@Service
@Transactional
public class RegistrarCanalUseCase {

    private final CanalRepository canales;
    private final CredencialesCanalService credenciales;

    public RegistrarCanalUseCase(CanalRepository canales, CredencialesCanalService credenciales) {
        this.canales = canales;
        this.credenciales = credenciales;
    }

    public CanalRegistrado ejecutar(String nombre, CanalOrigen tipo) {
        Canal canal = Canal.registrar(new IdCanal(UUID.randomUUID()), nombre, tipo);
        canales.guardar(canal);
        return new CanalRegistrado(canal, credenciales.emitir(canal.id()));
    }

    public record CanalRegistrado(Canal canal, String credencial) {
    }
}

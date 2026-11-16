package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.security.CredencialesCanalService;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CredencialCanalEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CredencialCanalJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Emite y valida la credencial del canal. Es aleatoria de 256 bits, así que basta su huella SHA-256
 * (a diferencia de una contraseña, no hace falta BCrypt) y se puede buscar por ella en cada petición.
 */
@Service
@Transactional
public class CredencialesCanalServiceImpl implements CredencialesCanalService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final CredencialCanalJpaRepository credenciales;
    private final Clock reloj;

    public CredencialesCanalServiceImpl(CredencialCanalJpaRepository credenciales, Clock reloj) {
        this.credenciales = credenciales;
        this.reloj = reloj;
    }

    @Override
    public String emitir(IdCanal canal) {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String credencial = "sga_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        registrar(canal, credencial);
        return credencial;
    }

    /** Guarda una credencial dada (la del simulador del curso, 12.3). */
    public void registrar(IdCanal canal, String credencial) {
        credenciales.save(new CredencialCanalEntity(canal.valor(), huella(credencial), true, LocalDateTime.now(reloj)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaActiva(IdCanal canal) {
        return credenciales.findById(canal.valor()).map(CredencialCanalEntity::isActiva).orElse(false);
    }

    /** El canal dueño de la credencial, si existe y está activa. */
    @Transactional(readOnly = true)
    public Optional<IdCanal> autenticar(String credencial) {
        if (credencial == null || credencial.isBlank()) {
            return Optional.empty();
        }
        return credenciales.findByHuellaAndActivaTrue(huella(credencial.trim()))
                .map(entidad -> new IdCanal(entidad.getCanal()));
    }

    static String huella(String credencial) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(credencial.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}

package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica que el usuario autenticado sea el titular de la reserva (Guía 12, 7.3; proyecto 7.2).
 * Lee el dominio a través de su puerto, pero el dominio no se entera: {@code Reserva} sigue sin saber
 * que existen usuarios. No decide ninguna regla de negocio: solo responde quién puede ver qué.
 */
@Component("propietarioReserva")
public class PropietarioReservaVerificador {

    private final ReservaRepository reservas;

    public PropietarioReservaVerificador(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public boolean esTitular(String codigo, Authentication authentication) {
        if (codigo == null || codigo.isBlank() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        String documento = jwt.getClaimAsString("documento");
        if (documento == null) {
            return false;
        }
        // Si la reserva no existe también se niega: un huésped no puede averiguar qué códigos
        // existen comparando 403 con 404.
        return reservas.buscarPorCodigo(new CodigoReserva(codigo))
                .flatMap(reserva -> reserva.titular().documento())
                .map(DocumentoIdentidad::numero)
                .map(documento::equals)
                .orElse(false);
    }
}

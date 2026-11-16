package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** Autenticación de un canal externo por su credencial (X-API-Key). Su única autoridad es CANAL_EXTERNO. */
public class CanalExternoAutenticado extends AbstractAuthenticationToken {

    public static final String AUTORIDAD = "CANAL_EXTERNO";

    private final IdCanal canal;

    public CanalExternoAutenticado(IdCanal canal) {
        super(List.of(new SimpleGrantedAuthority(AUTORIDAD)));
        this.canal = canal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public IdCanal getPrincipal() {
        return canal;
    }
}

package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;

/** Puerto de autenticación (Guía 12, 6.2). El controller depende de él, no de Spring Security. */
public interface SecurityService {

    TokenResponse login(LoginRequest request);
}

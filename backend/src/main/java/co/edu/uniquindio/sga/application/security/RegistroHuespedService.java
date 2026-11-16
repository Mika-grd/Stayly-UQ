package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.application.dto.request.RegistroHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;

/** Puerto del registro público de huéspedes (Guía 12, actividad 7.5.2; proyecto 7.2). */
public interface RegistroHuespedService {

    UsuarioResponse registrar(RegistroHuespedRequest request);
}

package co.edu.uniquindio.sga.application.security;

import co.edu.uniquindio.sga.domain.model.canal.IdCanal;

/**
 * Puerto de la credencial del canal externo (7.8): concepto de acceso, como los usuarios, así que el
 * dominio no la conoce. Se guarda solo su huella; el valor en claro se muestra una única vez.
 */
public interface CredencialesCanalService {

    /** Genera la credencial del canal y devuelve su valor en claro (no se vuelve a mostrar). */
    String emitir(IdCanal canal);

    boolean estaActiva(IdCanal canal);
}

package co.edu.uniquindio.sga.domain.repository;

import java.util.Optional;

/**
 * Puerto del modelo de inteligencia artificial (10.2). Redacta un texto a partir de una instrucción y
 * unos datos. Es no bloqueante por contrato: si el modelo está apagado, falla o tarda más que su
 * tiempo máximo, devuelve vacío y quien lo usa sigue sin él. Nunca es requisito para reservar,
 * cobrar ni registrar a un huésped.
 */
public interface AsistenteIA {

    Optional<String> redactar(String instruccion, String datos);

    /** Nombre del proveedor configurado, para mostrar de dónde salió un texto ("ninguno" si está apagado). */
    String proveedor();
}

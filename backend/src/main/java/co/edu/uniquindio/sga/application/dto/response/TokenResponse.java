package co.edu.uniquindio.sga.application.dto.response;

/**
 * DTO de salida del inicio de sesión (Guía 12, schema TokenResponse del openapi.yaml).
 *
 * @param token    JWT firmado con HS256
 * @param tipo     siempre "Bearer"
 * @param expiraEn segundos de vigencia del token desde su emisión
 * @param rol      rol del usuario autenticado (HUESPED, RECEPCIONISTA, ADMINISTRADOR, PERSONAL_SERVICIO)
 */
public record TokenResponse(String token, String tipo, long expiraEn, String rol) {
}

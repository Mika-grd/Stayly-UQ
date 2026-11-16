package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de ConflictoCanalResponse (schema PaginaConflictoCanalResponse, 10 elementos por defecto, 12.4). */
public record PaginaConflictoCanalResponse(List<ConflictoCanalResponse> contenido, int pagina, int tamano,
                                           long totalElementos, int totalPaginas) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de EventoCanalResponse (schema PaginaEventoCanalResponse, 10 elementos por defecto, 12.4). */
public record PaginaEventoCanalResponse(List<EventoCanalResponse> contenido, int pagina, int tamano,
                                        long totalElementos, int totalPaginas) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de ReservaResumenResponse (schema PaginaReservaResumenResponse, 10 elementos por defecto, 12.4). */
public record PaginaReservaResumenResponse(List<ReservaResumenResponse> contenido, int pagina, int tamano,
                                           long totalElementos, int totalPaginas) {
}

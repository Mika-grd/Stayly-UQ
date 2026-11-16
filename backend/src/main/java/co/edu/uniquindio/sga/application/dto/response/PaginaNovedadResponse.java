package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de novedades (schema PaginaNovedadResponse, 10 por página, 12.4). */
public record PaginaNovedadResponse(List<NovedadResponse> contenido, int pagina, int tamano, long totalElementos,
                                    int totalPaginas) {
}

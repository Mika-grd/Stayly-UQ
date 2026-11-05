package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de un listado (schema PaginaReservaResumenResponse y similares): de a 10 elementos (12.4). */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
}

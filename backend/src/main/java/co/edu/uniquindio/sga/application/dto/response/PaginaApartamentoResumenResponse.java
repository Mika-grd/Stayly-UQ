package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de ApartamentoResumenResponse (schema PaginaApartamentoResumenResponse, 10 elementos por defecto, 12.4). */
public record PaginaApartamentoResumenResponse(List<ApartamentoResumenResponse> contenido, int pagina, int tamano,
                                               long totalElementos, int totalPaginas) {
}

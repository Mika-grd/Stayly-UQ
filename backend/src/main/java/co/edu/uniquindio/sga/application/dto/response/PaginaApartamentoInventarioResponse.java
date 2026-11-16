package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página del inventario (schema PaginaApartamentoInventarioResponse, 10 por página, 12.4). */
public record PaginaApartamentoInventarioResponse(List<ApartamentoInventarioResponse> contenido, int pagina,
                                                  int tamano, long totalElementos, int totalPaginas) {
}

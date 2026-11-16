package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de usuarios (schema PaginaUsuarioResponse, 10 por página, 12.4). */
public record PaginaUsuarioResponse(List<UsuarioResponse> contenido, int pagina, int tamano, long totalElementos,
                                    int totalPaginas) {
}

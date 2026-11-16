package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;

/** Fila del inventario del administrador (schema ApartamentoInventarioResponse, 7.3). */
public record ApartamentoInventarioResponse(String identificacion, String nombre, int dormitorios, int capacidad,
                                            boolean activo, EstadoOperativo estadoOperativo,
                                            boolean tarifasCompletas) {
}

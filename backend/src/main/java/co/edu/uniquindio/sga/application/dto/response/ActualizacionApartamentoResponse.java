package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Resultado de actualizar un apartamento, con advertencia de capacidad (schema ActualizacionApartamentoResponse, 7.3). */
public record ActualizacionApartamentoResponse(ApartamentoDetalleResponse apartamento,
                                               List<String> reservasSobreNuevaCapacidad) {
}

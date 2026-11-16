package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.application.dto.ImagenDTO;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;

import java.util.List;

/** Detalle de un apartamento, con la ubicación y la dirección del alojamiento (schema ApartamentoDetalleResponse). */
public record ApartamentoDetalleResponse(String identificacion, String nombre, String descripcion, int dormitorios,
                                         int capacidad, List<String> caracteristicas, boolean admiteMascotas,
                                         String advertenciaAcceso, List<ImagenDTO> imagenes,
                                         EstadoOperativo estadoOperativo, boolean activo, UbicacionDTO ubicacion,
                                         String direccion) {
}

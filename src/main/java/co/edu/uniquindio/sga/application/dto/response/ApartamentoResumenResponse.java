package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Resumen de un apartamento para listados (schema ApartamentoResumenResponse). */
public record ApartamentoResumenResponse(String identificacion, String nombre, int capacidad, int dormitorios,
                                         String imagenPrincipal, List<String> caracteristicas,
                                         boolean admiteMascotas, String advertenciaAcceso, Long valorEstimado) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalTime;
import java.util.List;

/** Configuración vigente del alojamiento, recurso único (schema AlojamientoResponse, F-13). */
public record AlojamientoResponse(String nombre, String descripcion, String ciudad, String direccion,
                                  UbicacionDTO ubicacion, LocalTime horaEntrada, LocalTime horaSalida,
                                  List<String> normasConvivencia, List<ServicioAdicionalDTO> serviciosAdicionales,
                                  ParametrosAlojamientoDTO parametros, ParametrosReglasPropiasDTO reglasPropias) {
}

package co.edu.uniquindio.sga.application.dto;

/** Imagen del apartamento (schema ImagenDTO). Se usa tanto al crear/actualizar como en las consultas. */
public record ImagenDTO(String url, boolean principal) {
}

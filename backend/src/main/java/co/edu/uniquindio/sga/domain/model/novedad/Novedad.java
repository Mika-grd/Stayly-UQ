package co.edu.uniquindio.sga.domain.model.novedad;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raíz del agregado Novedad (soporte): daño, faltante o situación de un apartamento. Registrarla
 * no cambia el estado operativo (3.3). Invariante N-1.
 */
public class Novedad {

    private final IdNovedad id;
    private final IdentificacionApartamento apartamento;
    private final LocalDateTime fecha;
    private final String autor;
    private final String descripcion;
    private final GravedadNovedad gravedad;

    private Novedad(IdNovedad id, IdentificacionApartamento apartamento, LocalDateTime fecha, String autor,
                    String descripcion, GravedadNovedad gravedad) {
        this.id = id;
        this.apartamento = apartamento;
        this.fecha = fecha;
        this.autor = autor;
        this.descripcion = descripcion;
        this.gravedad = gravedad;
    }

    public static Novedad registrar(IdNovedad id, IdentificacionApartamento apartamento, String autor,
                                    String descripcion, GravedadNovedad gravedad, LocalDateTime ahora) {
        if (id == null || apartamento == null || ahora == null || gravedad == null
                || autor == null || autor.isBlank() || descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La novedad requiere fecha, autor, descripción y gravedad.");
        }
        return new Novedad(id, apartamento, ahora, autor.trim(), descripcion.trim(), gravedad);
    }

    public IdNovedad id() { return id; }
    public IdentificacionApartamento apartamento() { return apartamento; }
    public LocalDateTime fecha() { return fecha; }
    public String autor() { return autor; }
    public String descripcion() { return descripcion; }
    public GravedadNovedad gravedad() { return gravedad; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Novedad otra && id.equals(otra.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

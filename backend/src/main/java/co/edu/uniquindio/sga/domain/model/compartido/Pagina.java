package co.edu.uniquindio.sga.domain.model.compartido;

import java.util.List;
import java.util.function.Function;

/**
 * Una página de un listado (12.4): sus elementos, qué página es y cuántos hay en total. No depende
 * de ningún framework; los adaptadores traducen su paginación a este tipo.
 */
public record Pagina<T>(List<T> contenido, int numero, int tamano, long totalElementos) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    /** Pagina una lista ya filtrada y ordenada. */
    public static <T> Pagina<T> de(List<T> todos, PeticionPagina peticion) {
        int desde = Math.min(peticion.desplazamiento(), todos.size());
        int hasta = Math.min(desde + peticion.tamano(), todos.size());
        return new Pagina<>(todos.subList(desde, hasta), peticion.numero(), peticion.tamano(), todos.size());
    }

    public int totalPaginas() {
        return (int) ((totalElementos + tamano - 1) / tamano);
    }

    public <R> Pagina<R> map(Function<T, R> transformacion) {
        return new Pagina<>(contenido.stream().map(transformacion).toList(), numero, tamano, totalElementos);
    }
}

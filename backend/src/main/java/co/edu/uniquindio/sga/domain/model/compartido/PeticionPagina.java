package co.edu.uniquindio.sga.domain.model.compartido;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Qué página de un listado se pide. Todos los listados se paginan de a 10 (12.4). */
public record PeticionPagina(int numero) {

    public static final int TAMANO = 10;

    public PeticionPagina {
        if (numero < 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El número de página empieza en 0.");
        }
    }

    public static PeticionPagina primera() {
        return new PeticionPagina(0);
    }

    public int tamano() {
        return TAMANO;
    }

    public int desplazamiento() {
        return numero * TAMANO;
    }
}

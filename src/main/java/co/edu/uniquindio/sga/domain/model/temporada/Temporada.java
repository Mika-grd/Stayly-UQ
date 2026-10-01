package co.edu.uniquindio.sga.domain.model.temporada;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Temporada. Puede cubrir varios periodos del año (la alta: diciembre–enero y
 * Semana Santa). La base no tiene periodos: cubre las noches que no tienen otra temporada (F-05).
 * Invariante T-1; que no se solape con otras temporadas lo verifica CalendarioTemporadasService.
 */
public class Temporada {

    private final IdTemporada id;
    private final String nombre;
    private final List<RangoFechas> periodos;
    private final boolean esBase;
    private EstanciaMinima estanciaMinima;

    private Temporada(IdTemporada id, String nombre, List<RangoFechas> periodos, boolean esBase,
                      EstanciaMinima estanciaMinima) {
        if (id == null || nombre == null || nombre.isBlank() || estanciaMinima == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La temporada requiere identificador, nombre y estancia mínima.");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.periodos = new ArrayList<>();
        this.esBase = esBase;
        this.estanciaMinima = estanciaMinima;
        periodos.forEach(this::agregarPeriodo);
    }

    /** T-1: una temporada que no es base tiene al menos un periodo y sus periodos no se solapan. */
    public static Temporada crear(IdTemporada id, String nombre, List<RangoFechas> periodos,
                                  EstanciaMinima estanciaMinima) {
        if (periodos == null || periodos.isEmpty()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "Una temporada que no es base debe tener al menos un periodo.");
        }
        return new Temporada(id, nombre, periodos, false, estanciaMinima);
    }

    /** La base no exige estancia mínima (1 noche) hasta que el administrador la cambie. */
    public static Temporada crearBase(IdTemporada id, String nombre) {
        return new Temporada(id, nombre, List.of(), true, new EstanciaMinima(1));
    }

    public void agregarPeriodo(RangoFechas periodo) {
        if (esBase) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La temporada base no tiene periodos: cubre las fechas sin otra temporada.");
        }
        if (periodo == null) {
            throw new ReglaDominioException("RANGO_FECHAS_INVALIDO", "La fecha final debe ser posterior a la fecha inicial.");
        }
        if (periodos.stream().anyMatch(periodo::seSolapaCon)) {
            throw new ReglaDominioException("TEMPORADAS_SOLAPADAS",
                    "El periodo se cruza con otro periodo de la temporada " + nombre + ".");
        }
        periodos.add(periodo);
    }

    /** La base no contiene noches por sí misma: el repositorio la devuelve cuando ninguna otra aplica. */
    public boolean contiene(LocalDate noche) {
        return periodos.stream().anyMatch(periodo -> periodo.contiene(noche));
    }

    public boolean seSolapaCon(Temporada otra) {
        return periodos.stream().anyMatch(propio -> otra.periodos.stream().anyMatch(propio::seSolapaCon));
    }

    public void cambiarEstanciaMinima(EstanciaMinima nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La estancia mínima debe ser de al menos 1 noche.");
        }
        estanciaMinima = nueva;
    }

    public IdTemporada id() { return id; }
    public String nombre() { return nombre; }
    public List<RangoFechas> periodos() { return Collections.unmodifiableList(periodos); }
    public boolean esBase() { return esBase; }
    public EstanciaMinima estanciaMinima() { return estanciaMinima; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Temporada otra && id.equals(otra.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

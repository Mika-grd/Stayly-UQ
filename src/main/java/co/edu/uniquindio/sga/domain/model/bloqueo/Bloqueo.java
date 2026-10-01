package co.edu.uniquindio.sga.domain.model.bloqueo;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raíz del agregado Bloqueo (soporte). Mientras esté vigente impide vender las noches que cubre
 * (B-2, RN-07). No cambia el estado operativo del apartamento (3.3).
 */
public class Bloqueo {

    private final IdBloqueo id;
    private final IdentificacionApartamento apartamento;
    private final RangoFechas rango;
    private final String motivo;
    private final String autor;
    private final LocalDateTime fechaRegistro;
    private boolean levantado;

    private Bloqueo(IdBloqueo id, IdentificacionApartamento apartamento, RangoFechas rango, String motivo,
                    String autor, LocalDateTime fechaRegistro) {
        this.id = id;
        this.apartamento = apartamento;
        this.rango = rango;
        this.motivo = motivo;
        this.autor = autor;
        this.fechaRegistro = fechaRegistro;
        this.levantado = false;
    }

    /** B-1: rango de al menos una noche (lo garantiza RangoFechas) y motivo no vacío. */
    public static Bloqueo registrar(IdBloqueo id, IdentificacionApartamento apartamento, RangoFechas rango,
                                    String motivo, String autor, LocalDateTime ahora) {
        if (id == null || apartamento == null || rango == null || ahora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El bloqueo requiere apartamento, rango y fecha.");
        }
        if (motivo == null || motivo.isBlank() || autor == null || autor.isBlank()) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El bloqueo requiere motivo y autor.");
        }
        return new Bloqueo(id, apartamento, rango, motivo.trim(), autor.trim(), ahora);
    }

    public boolean afecta(Estancia estancia) {
        return !levantado && rango.seSolapaCon(estancia);
    }

    public void levantar() {
        levantado = true;
    }

    public IdBloqueo id() { return id; }
    public IdentificacionApartamento apartamento() { return apartamento; }
    public RangoFechas rango() { return rango; }
    public String motivo() { return motivo; }
    public String autor() { return autor; }
    public LocalDateTime fechaRegistro() { return fechaRegistro; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Bloqueo otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

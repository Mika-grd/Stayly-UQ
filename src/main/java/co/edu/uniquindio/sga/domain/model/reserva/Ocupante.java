package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Entidad interna del agregado Reserva. Se registra su fecha de nacimiento, no su edad (3.2).
 * El titular es el ocupante marcado con {@code titular = true}: lleva documento y contacto.
 */
public class Ocupante {

    private final IdOcupante id;
    private final String nombre;
    private final LocalDate fechaNacimiento;
    private final DocumentoIdentidad documento;
    private final boolean titular;
    private final DatosContactoTitular contacto;

    private Ocupante(IdOcupante id, String nombre, LocalDate fechaNacimiento, DocumentoIdentidad documento,
                     boolean titular, DatosContactoTitular contacto) {
        if (id == null || nombre == null || nombre.isBlank() || fechaNacimiento == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "El ocupante requiere identificador, nombre y fecha de nacimiento.");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.documento = documento;
        this.titular = titular;
        this.contacto = contacto;
    }

    /** Ocupante que no es titular. El documento es opcional (un menor puede no tenerlo). */
    public static Ocupante acompanante(IdOcupante id, String nombre, LocalDate nacimiento, DocumentoIdentidad documento) {
        return new Ocupante(id, nombre, nacimiento, documento, false, null);
    }

    public static Ocupante titular(IdOcupante id, String nombre, LocalDate nacimiento, DocumentoIdentidad documento,
                                   DatosContactoTitular contacto) {
        if (documento == null || contacto == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "El titular debe tener documento de identidad y datos de contacto.");
        }
        return new Ocupante(id, nombre, nacimiento, documento, true, contacto);
    }

    /** RN-06: facturable si a la fecha de entrada alcanza el umbral; no cambia durante la estancia. */
    public boolean esFacturableEn(LocalDate entrada, UmbralEdadFacturable umbral) {
        return umbral.alcanzadoPor(fechaNacimiento, entrada);
    }

    public boolean esTitular() {
        return titular;
    }

    public IdOcupante id() { return id; }
    public String nombre() { return nombre; }
    public LocalDate fechaNacimiento() { return fechaNacimiento; }
    public Optional<DocumentoIdentidad> documento() { return Optional.ofNullable(documento); }
    public Optional<DatosContactoTitular> contacto() { return Optional.ofNullable(contacto); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Ocupante otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

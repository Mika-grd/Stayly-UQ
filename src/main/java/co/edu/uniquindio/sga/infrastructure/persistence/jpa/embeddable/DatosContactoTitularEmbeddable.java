package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Objeto de valor DatosContactoTitular: correo y teléfono, que van siempre juntos. */
@Embeddable
public class DatosContactoTitularEmbeddable {

    @Column(name = "contacto_correo", length = 150)
    private String correo;

    @Column(name = "contacto_telefono", length = 30)
    private String telefono;

    protected DatosContactoTitularEmbeddable() {
    }

    public DatosContactoTitularEmbeddable(String correo, String telefono) {
        this.correo = correo;
        this.telefono = telefono;
    }

    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
}

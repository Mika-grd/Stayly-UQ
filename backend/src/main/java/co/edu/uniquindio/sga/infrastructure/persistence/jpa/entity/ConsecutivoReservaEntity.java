package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Último número de reserva emitido en un año: de aquí sale el código RES-AAAA-NNNNN. */
@Entity
@Table(name = "consecutivo_reserva")
public class ConsecutivoReservaEntity {

    @Id
    private int anio;

    @Column(nullable = false)
    private int ultimo;

    protected ConsecutivoReservaEntity() {
    }

    public ConsecutivoReservaEntity(int anio) {
        this.anio = anio;
        this.ultimo = 0;
    }

    /** Avanza el consecutivo y devuelve el nuevo número. */
    public int siguiente() {
        return ++ultimo;
    }

    public int getAnio() { return anio; }
}

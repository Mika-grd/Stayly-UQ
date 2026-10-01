package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado ConflictoCanal (soporte): una reserva externa rechazada porque chocaba con
 * una reserva vigente (RN-18). CC-1: solo pasa de PENDIENTE a RESUELTO, con autor y resolución.
 */
public class ConflictoCanal {

    private final IdConflictoCanal id;
    private final IdentificadorExterno identificadorExterno;
    private final IdentificacionApartamento apartamento;
    private final Estancia estancia;
    private final CodigoReserva reservaVigente;
    private EstadoConflicto estado;
    private String resolucion;
    private String autorResolucion;
    private LocalDateTime fechaResolucion;
    private final LocalDateTime fechaRegistro;

    private ConflictoCanal(IdConflictoCanal id, IdentificadorExterno identificadorExterno,
                           IdentificacionApartamento apartamento, Estancia estancia, CodigoReserva reservaVigente,
                           LocalDateTime fechaRegistro) {
        this.id = id;
        this.identificadorExterno = identificadorExterno;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.reservaVigente = reservaVigente;
        this.estado = EstadoConflicto.PENDIENTE;
        this.fechaRegistro = fechaRegistro;
    }

    public static ConflictoCanal registrar(IdConflictoCanal id, IdentificadorExterno identificadorExterno,
                                           IdentificacionApartamento apartamento, Estancia estancia,
                                           CodigoReserva reservaVigente, LocalDateTime ahora) {
        if (id == null || identificadorExterno == null || apartamento == null || estancia == null
                || reservaVigente == null || ahora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Faltan datos para registrar el conflicto de canal.");
        }
        return new ConflictoCanal(id, identificadorExterno, apartamento, estancia, reservaVigente, ahora);
    }

    public void resolver(String autor, String resolucion, LocalDateTime ahora) {
        if (estado == EstadoConflicto.RESUELTO) {
            throw new ReglaDominioException("CONFLICTO_YA_RESUELTO", "El conflicto ya fue resuelto.");
        }
        if (autor == null || autor.isBlank() || resolucion == null || resolucion.isBlank() || ahora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La resolución del conflicto requiere autor y descripción.");
        }
        this.estado = EstadoConflicto.RESUELTO;
        this.autorResolucion = autor.trim();
        this.resolucion = resolucion.trim();
        this.fechaResolucion = ahora;
    }

    public IdConflictoCanal id() { return id; }
    public IdentificadorExterno identificadorExterno() { return identificadorExterno; }
    public IdentificacionApartamento apartamento() { return apartamento; }
    public Estancia estancia() { return estancia; }
    public CodigoReserva reservaVigente() { return reservaVigente; }
    public EstadoConflicto estado() { return estado; }
    public Optional<String> resolucion() { return Optional.ofNullable(resolucion); }
    public Optional<String> autorResolucion() { return Optional.ofNullable(autorResolucion); }
    public Optional<LocalDateTime> fechaResolucion() { return Optional.ofNullable(fechaResolucion); }
    public LocalDateTime fechaRegistro() { return fechaRegistro; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ConflictoCanal otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Raíz del agregado Canal (soporte). C-1: la bitácora solo crece. */
public class Canal {

    private final IdCanal id;
    private final String nombre;
    private final CanalOrigen tipo;
    private final List<EventoCanal> eventos = new ArrayList<>();

    private Canal(IdCanal id, String nombre, CanalOrigen tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public static Canal registrar(IdCanal id, String nombre, CanalOrigen tipo) {
        if (id == null || nombre == null || nombre.isBlank() || tipo == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El canal requiere identificador, nombre y tipo.");
        }
        return new Canal(id, nombre.trim(), tipo);
    }

    public void registrarEvento(EventoCanal evento) {
        if (evento == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El evento del canal es obligatorio.");
        }
        eventos.add(evento);
    }

    public IdCanal id() { return id; }
    public String nombre() { return nombre; }
    public CanalOrigen tipo() { return tipo; }
    public List<EventoCanal> eventos() { return Collections.unmodifiableList(eventos); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Canal otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

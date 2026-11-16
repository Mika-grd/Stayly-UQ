package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;

/** Un conflicto con el nombre de su canal, que el administrador necesita para revisarlo (7.8). */
public record ConflictoConCanal(ConflictoCanal conflicto, String nombreCanal) {
}

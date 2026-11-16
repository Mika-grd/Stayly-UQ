package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;

import java.util.List;

public interface ConflictoCanalRepository {

    void guardar(ConflictoCanal conflicto);

    List<ConflictoCanal> listarPendientes();
}

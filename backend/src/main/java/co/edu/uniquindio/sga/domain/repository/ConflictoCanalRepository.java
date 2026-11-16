package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;

import java.util.List;
import java.util.Optional;

public interface ConflictoCanalRepository {

    void guardar(ConflictoCanal conflicto);

    Optional<ConflictoCanal> buscarPorId(IdConflictoCanal id);

    List<ConflictoCanal> listarPendientes();

    /** Revisión del administrador (7.8): del más antiguo al más reciente; sin estado trae todos. */
    Pagina<ConflictoCanal> buscar(EstadoConflicto estado, PeticionPagina pagina);
}

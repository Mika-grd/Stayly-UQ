package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 7.8, RN-18: los conflictos con canales externos para que el administrador los revise; sin estado trae todos. */
@Service
@Transactional(readOnly = true)
public class ConsultarConflictosCanalUseCase {

    private final ConflictoCanalRepository conflictos;
    private final CanalRepository canales;

    public ConsultarConflictosCanalUseCase(ConflictoCanalRepository conflictos, CanalRepository canales) {
        this.conflictos = conflictos;
        this.canales = canales;
    }

    public Pagina<ConflictoConCanal> ejecutar(EstadoConflicto estado, PeticionPagina pagina) {
        Map<IdCanal, String> nombres = canales.listar().stream()
                .collect(Collectors.toMap(Canal::id, Canal::nombre));
        Function<IdCanal, String> nombre = id -> nombres.getOrDefault(id, id.valor().toString());
        return conflictos.buscar(estado, pagina)
                .map(conflicto -> new ConflictoConCanal(conflicto,
                        nombre.apply(conflicto.identificadorExterno().canal())));
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdConflictoCanal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** 7.8: PENDIENTE → RESUELTO, con quién lo resolvió y qué se hizo con el caso (CC-1). */
@Service
@Transactional
public class ResolverConflictoCanalUseCase {

    private final ConflictoCanalRepository conflictos;
    private final CanalRepository canales;
    private final Clock reloj;

    public ResolverConflictoCanalUseCase(ConflictoCanalRepository conflictos, CanalRepository canales, Clock reloj) {
        this.conflictos = conflictos;
        this.canales = canales;
        this.reloj = reloj;
    }

    public ConflictoConCanal ejecutar(IdConflictoCanal id, String autor, String resolucion) {
        ConflictoCanal conflicto = Consultas.conflicto(conflictos, id);
        conflicto.resolver(autor, resolucion, LocalDateTime.now(reloj));
        conflictos.guardar(conflicto);
        String nombreCanal = canales.buscarPorId(conflicto.identificadorExterno().canal())
                .map(canal -> canal.nombre())
                .orElse(conflicto.identificadorExterno().canal().valor().toString());
        return new ConflictoConCanal(conflicto, nombreCanal);
    }
}

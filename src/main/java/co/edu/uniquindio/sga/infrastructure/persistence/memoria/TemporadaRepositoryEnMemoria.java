package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemporadaRepositoryEnMemoria implements TemporadaRepository {

    private final Map<IdTemporada, Temporada> temporadas = new HashMap<>();

    @Override
    public void guardar(Temporada temporada) {
        temporadas.put(temporada.id(), temporada);
    }

    @Override
    public List<Temporada> listarTodas() {
        return List.copyOf(temporadas.values());
    }

    @Override
    public Temporada buscarTemporadaDe(LocalDate noche) {
        return temporadas.values().stream()
                .filter(temporada -> temporada.contiene(noche))
                .findFirst()
                .orElseGet(this::buscarBase);
    }

    @Override
    public Temporada buscarBase() {
        return temporadas.values().stream()
                .filter(Temporada::esBase)
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("TEMPORADA_NO_ENCONTRADA",
                        "No existe una temporada base."));
    }
}

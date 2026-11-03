package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TemporadaMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TemporadaJpaRepository;

import java.time.LocalDate;
import java.util.List;

/** Adaptador JPA del puerto TemporadaRepository. */
public class TemporadaRepositoryJpa implements TemporadaRepository {

    private final TemporadaJpaRepository jpa;

    public TemporadaRepositoryJpa(TemporadaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Temporada temporada) {
        jpa.save(TemporadaMapper.aEntidad(temporada));
    }

    @Override
    public List<Temporada> listarTodas() {
        return jpa.findAll().stream().map(TemporadaMapper::aDominio).toList();
    }

    /** CalendarioTemporadasService garantiza que las temporadas no se solapan: a lo sumo una contiene la noche. */
    @Override
    public Temporada buscarTemporadaDe(LocalDate noche) {
        return jpa.buscarQueContienen(noche).stream()
                .findFirst()
                .map(TemporadaMapper::aDominio)
                .orElseGet(this::buscarBase);
    }

    @Override
    public Temporada buscarBase() {
        return jpa.findFirstByEsBaseTrue()
                .map(TemporadaMapper::aDominio)
                .orElseThrow(() -> new ReglaDominioException("TEMPORADA_NO_ENCONTRADA",
                        "No existe una temporada base."));
    }
}

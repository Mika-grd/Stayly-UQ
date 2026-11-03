package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TemporadaRepository {

    void guardar(Temporada temporada);

    List<Temporada> listarTodas();

    Optional<Temporada> buscarPorId(IdTemporada id);

    /** La temporada que contiene esa noche o, si ninguna la contiene, la temporada base (F-05). */
    Temporada buscarTemporadaDe(LocalDate noche);

    Temporada buscarBase();
}

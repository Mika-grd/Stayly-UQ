package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.temporada.Temporada;

import java.time.LocalDate;
import java.util.List;

public interface TemporadaRepository {

    void guardar(Temporada temporada);

    List<Temporada> listarTodas();

    /** La temporada que contiene esa noche o, si ninguna la contiene, la temporada base (F-05). */
    Temporada buscarTemporadaDe(LocalDate noche);

    Temporada buscarBase();
}

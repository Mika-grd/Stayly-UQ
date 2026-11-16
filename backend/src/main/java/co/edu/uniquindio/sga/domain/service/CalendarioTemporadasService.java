package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

/** 7.4 y F-05: las temporadas no se solapan y existe exactamente una base. Depende de todas. */
public class CalendarioTemporadasService {

    private final TemporadaRepository temporadas;

    public CalendarioTemporadasService(TemporadaRepository temporadas) {
        this.temporadas = temporadas;
    }

    public void verificarCalendario(Temporada nueva) {
        for (Temporada existente : temporadas.listarTodas()) {
            if (existente.equals(nueva)) {
                continue;
            }
            if (nueva.esBase() && existente.esBase()) {
                throw new ReglaDominioException("TEMPORADA_BASE_DUPLICADA", "Ya existe una temporada base.");
            }
            if (nueva.seSolapaCon(existente)) {
                throw new ReglaDominioException("TEMPORADAS_SOLAPADAS",
                        "La temporada se solapa con la temporada " + existente.nombre() + ".");
            }
        }
    }
}

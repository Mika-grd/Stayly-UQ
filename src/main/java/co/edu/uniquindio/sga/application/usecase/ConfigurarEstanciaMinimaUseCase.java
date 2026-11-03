package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RP-01: noches mínimas que exige una temporada; se cambia por configuración, sin tocar el código. */
@Service
@Transactional
public class ConfigurarEstanciaMinimaUseCase {

    private final TemporadaRepository temporadas;

    public ConfigurarEstanciaMinimaUseCase(TemporadaRepository temporadas) {
        this.temporadas = temporadas;
    }

    public Temporada ejecutar(IdTemporada id, EstanciaMinima estanciaMinima) {
        Temporada temporada = Consultas.temporada(temporadas, id);
        temporada.cambiarEstanciaMinima(estanciaMinima);
        temporadas.guardar(temporada);
        return temporada;
    }
}

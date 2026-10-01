package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

/** RP-01: la estancia mínima la fija la temporada de la noche de entrada (otro agregado). */
public class VerificadorEstanciaMinimaService {

    private final TemporadaRepository temporadas;

    public VerificadorEstanciaMinimaService(TemporadaRepository temporadas) {
        this.temporadas = temporadas;
    }

    public void verificarEstanciaMinima(Estancia estancia) {
        Temporada temporada = temporadas.buscarTemporadaDe(estancia.entrada());
        if (!temporada.estanciaMinima().seCumpleCon(estancia)) {
            throw new ReglaDominioException("ESTANCIA_MINIMA_NO_CUMPLIDA",
                    "La temporada exige una estancia mínima de " + temporada.estanciaMinima().noches() + " noches.");
        }
    }
}

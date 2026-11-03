package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

/** 7.4: activar o reservar un apartamento exige tarifa en todas las temporadas. */
public class VerificadorTarifasCompletasService {

    private final TemporadaRepository temporadas;
    private final TarifarioRepository tarifarios;

    public VerificadorTarifasCompletasService(TemporadaRepository temporadas, TarifarioRepository tarifarios) {
        this.temporadas = temporadas;
        this.tarifarios = tarifarios;
    }

    public void verificarTarifasCompletas(IdentificacionApartamento apartamento) {
        if (!tieneTarifasCompletas(apartamento)) {
            throw new ReglaDominioException("TARIFAS_INCOMPLETAS",
                    "El apartamento " + apartamento + " no tiene tarifa en todas las temporadas.");
        }
    }

    /** Si tiene tarifa vigente en cada una de las temporadas definidas, base incluida. */
    public boolean tieneTarifasCompletas(IdentificacionApartamento apartamento) {
        Tarifario tarifario = tarifarios.buscarPorApartamento(apartamento).orElse(null);
        return tarifario != null && temporadas.listarTodas().stream()
                .allMatch(temporada -> tarifario.tieneTarifaPara(temporada.id()));
    }
}

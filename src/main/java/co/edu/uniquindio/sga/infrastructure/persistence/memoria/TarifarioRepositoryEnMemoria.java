package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TarifarioRepositoryEnMemoria implements TarifarioRepository {

    private final Map<IdentificacionApartamento, Tarifario> tarifarios = new HashMap<>();

    @Override
    public void guardar(Tarifario tarifario) {
        tarifarios.put(tarifario.apartamento(), tarifario);
    }

    @Override
    public Optional<Tarifario> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return Optional.ofNullable(tarifarios.get(apartamento));
    }
}

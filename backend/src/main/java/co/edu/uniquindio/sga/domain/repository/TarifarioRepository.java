package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;

import java.util.Optional;

public interface TarifarioRepository {

    void guardar(Tarifario tarifario);

    Optional<Tarifario> buscarPorApartamento(IdentificacionApartamento apartamento);
}

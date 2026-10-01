package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

public interface ApartamentoRepository {

    void guardar(Apartamento apartamento);

    Optional<Apartamento> buscarPorIdentificacion(IdentificacionApartamento identificacion);

    List<Apartamento> listarActivos();
}

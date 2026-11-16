package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApartamentoRepositoryEnMemoria implements ApartamentoRepository {

    private final Map<IdentificacionApartamento, Apartamento> apartamentos = new HashMap<>();

    @Override
    public void guardar(Apartamento apartamento) {
        apartamentos.put(apartamento.identificacion(), apartamento);
    }

    @Override
    public Optional<Apartamento> buscarPorIdentificacion(IdentificacionApartamento identificacion) {
        return Optional.ofNullable(apartamentos.get(identificacion));
    }

    @Override
    public List<Apartamento> listarActivos() {
        return apartamentos.values().stream().filter(Apartamento::estaActivo).toList();
    }
}

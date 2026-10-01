package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.IdNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NovedadRepositoryEnMemoria implements NovedadRepository {

    private final Map<IdNovedad, Novedad> novedades = new HashMap<>();

    @Override
    public void guardar(Novedad novedad) {
        novedades.put(novedad.id(), novedad);
    }

    @Override
    public List<Novedad> listarPorApartamento(IdentificacionApartamento apartamento) {
        return novedades.values().stream()
                .filter(novedad -> novedad.apartamento().equals(apartamento))
                .sorted(Comparator.comparing(Novedad::fecha))
                .toList();
    }
}

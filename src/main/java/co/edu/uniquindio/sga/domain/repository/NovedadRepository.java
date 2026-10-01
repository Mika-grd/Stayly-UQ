package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;

import java.util.List;

public interface NovedadRepository {

    void guardar(Novedad novedad);

    List<Novedad> listarPorApartamento(IdentificacionApartamento apartamento);
}

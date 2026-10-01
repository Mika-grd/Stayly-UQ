package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;

import java.util.List;

public interface BloqueoRepository {

    void guardar(Bloqueo bloqueo);

    List<Bloqueo> buscarVigentesQueSolapan(IdentificacionApartamento apartamento, Estancia estancia);
}

package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;

import java.util.List;

public interface NovedadRepository {

    void guardar(Novedad novedad);

    List<Novedad> listarPorApartamento(IdentificacionApartamento apartamento);

    /** Novedades del apartamento, de la más reciente a la más antigua, de a 10 (12.4). */
    Pagina<Novedad> buscarPorApartamento(IdentificacionApartamento apartamento, PeticionPagina pagina);
}

package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;

public interface PoliticaCancelacionRepository {

    void guardar(PoliticaCancelacion politica);

    /** La versión de mayor número. */
    PoliticaCancelacion buscarVigente();

    PoliticaCancelacion buscarPorVersion(VersionPolitica version);
}

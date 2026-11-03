package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.PoliticaCancelacionMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.PoliticaCancelacionJpaRepository;

/** Adaptador JPA del puerto PoliticaCancelacionRepository. */
public class PoliticaCancelacionRepositoryJpa implements PoliticaCancelacionRepository {

    private final PoliticaCancelacionJpaRepository jpa;

    public PoliticaCancelacionRepositoryJpa(PoliticaCancelacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(PoliticaCancelacion politica) {
        jpa.save(PoliticaCancelacionMapper.aEntidad(politica));
    }

    @Override
    public PoliticaCancelacion buscarVigente() {
        return jpa.findFirstByOrderByVersionDesc()
                .map(PoliticaCancelacionMapper::aDominio)
                .orElseThrow(() -> new ReglaDominioException("POLITICA_NO_ENCONTRADA",
                        "No existe una política de cancelación vigente."));
    }

    @Override
    public PoliticaCancelacion buscarPorVersion(VersionPolitica version) {
        return jpa.findById(version.numero())
                .map(PoliticaCancelacionMapper::aDominio)
                .orElseThrow(() -> new ReglaDominioException("POLITICA_NO_ENCONTRADA",
                        "No existe la versión " + version.numero() + " de la política de cancelación."));
    }
}

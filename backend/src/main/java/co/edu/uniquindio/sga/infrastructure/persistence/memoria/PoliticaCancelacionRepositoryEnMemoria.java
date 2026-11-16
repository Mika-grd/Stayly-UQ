package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PoliticaCancelacionRepositoryEnMemoria implements PoliticaCancelacionRepository {

    private final Map<VersionPolitica, PoliticaCancelacion> versiones = new HashMap<>();

    @Override
    public void guardar(PoliticaCancelacion politica) {
        versiones.put(politica.version(), politica);
    }

    @Override
    public PoliticaCancelacion buscarVigente() {
        return versiones.values().stream()
                .max(Comparator.comparingInt(politica -> politica.version().numero()))
                .orElseThrow(() -> new ReglaDominioException("POLITICA_NO_ENCONTRADA",
                        "No existe una política de cancelación vigente."));
    }

    @Override
    public PoliticaCancelacion buscarPorVersion(VersionPolitica version) {
        return Optional.ofNullable(versiones.get(version))
                .orElseThrow(() -> new ReglaDominioException("POLITICA_NO_ENCONTRADA",
                        "No existe la versión " + version.numero() + " de la política de cancelación."));
    }
}

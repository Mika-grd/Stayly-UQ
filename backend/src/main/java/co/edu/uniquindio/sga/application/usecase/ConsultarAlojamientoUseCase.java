package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-22 (7.3): datos del alojamiento, horarios, normas, servicios y parámetros de la sección 6. Los
 * valores iniciales vienen de application.properties (se cambian sin recompilar) y el administrador
 * los modifica con CU-36 y CU-37.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarAlojamientoUseCase {

    private final ConfiguracionAlojamiento configuracion;

    public ConsultarAlojamientoUseCase(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public Alojamiento ejecutar() {
        return configuracion.alojamiento();
    }
}

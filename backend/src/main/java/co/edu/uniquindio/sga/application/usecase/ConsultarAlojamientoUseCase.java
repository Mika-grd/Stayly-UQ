package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 7.3: datos del alojamiento, horarios, normas, servicios y parámetros de la sección 6. Es de solo
 * consulta: los valores se cambian en application.properties, sin recompilar.
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

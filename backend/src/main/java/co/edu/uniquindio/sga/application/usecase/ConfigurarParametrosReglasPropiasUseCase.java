package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.CargoPorMascota;
import co.edu.uniquindio.sga.domain.model.alojamiento.CupoMascotas;
import co.edu.uniquindio.sga.domain.model.alojamiento.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-37: el administrador cambia los parámetros de las reglas propias RP-02 (cupo y cargo por
 * mascota) y RP-03 (hora de inicio y valor del recargo por llegada nocturna). Las reservas ya
 * creadas conservan sus cargos; el valor nuevo se aplica desde la siguiente operación.
 */
@Service
@Transactional
public class ConfigurarParametrosReglasPropiasUseCase {

    private final ConfiguracionAlojamiento configuracion;

    public ConfigurarParametrosReglasPropiasUseCase(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public Alojamiento ejecutar(CupoMascotas cupoMascotas, CargoPorMascota cargoPorMascota,
                                RecargoLlegadaNocturna recargoLlegadaNocturna) {
        Alojamiento alojamiento = configuracion.alojamiento();
        alojamiento.actualizarReglasPropias(cupoMascotas, cargoPorMascota, recargoLlegadaNocturna);
        configuracion.guardar(alojamiento);
        return alojamiento;
    }
}

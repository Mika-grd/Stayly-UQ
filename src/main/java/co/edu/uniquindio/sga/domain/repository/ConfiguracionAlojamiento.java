package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.alojamiento.CargoPorMascota;
import co.edu.uniquindio.sga.domain.model.alojamiento.CupoMascotas;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.model.alojamiento.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;

/**
 * Puerto del dominio para leer la configuración vigente del Alojamiento (valores de la Ficha,
 * Anexo A). Los servicios y casos de uso lo reciben por constructor: ningún valor se escribe
 * en el dominio.
 */
public interface ConfiguracionAlojamiento {

    UmbralEdadFacturable umbralEdad();

    TiempoPreparacion tiempoPreparacion();

    HorarioAlojamiento horario();

    PlazoConfirmacion plazoConfirmacion();

    HoraLimiteNoShow horaLimiteNoShow();

    Anticipo anticipo();

    CupoMascotas cupoMascotas();

    CargoPorMascota cargoPorMascota();

    RecargoLlegadaNocturna recargoLlegadaNocturna();
}

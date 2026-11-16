package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.ServicioAdicional;
import co.edu.uniquindio.sga.domain.model.alojamiento.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.Ubicacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * CU-36 (7.3, F-13): el administrador reemplaza los datos del alojamiento y los parámetros de la
 * sección 6. Alojamiento valida (nombre, ubicación, dos medios de pago, rangos de cada parámetro);
 * los parámetros de las reglas propias se conservan (los cambia CU-37).
 */
@Service
@Transactional
public class ConfigurarAlojamientoUseCase {

    private final ConfiguracionAlojamiento configuracion;

    public ConfigurarAlojamientoUseCase(ConfiguracionAlojamiento configuracion) {
        this.configuracion = configuracion;
    }

    public Alojamiento ejecutar(Comando comando) {
        Alojamiento alojamiento = configuracion.alojamiento();
        alojamiento.actualizarDatos(comando.nombre(), comando.descripcion(), comando.ciudad(), comando.direccion(),
                comando.ubicacion(), comando.normasConvivencia(), comando.servicios(), comando.mediosPago());
        alojamiento.actualizarParametros(comando.horario(), comando.umbralEdad(), comando.tiempoPreparacion(),
                comando.plazoConfirmacion(), comando.horaLimiteNoShow(), comando.anticipo(),
                alojamiento.cupoMascotas(), alojamiento.cargoPorMascota(), alojamiento.recargoLlegadaNocturna());
        configuracion.guardar(alojamiento);
        return alojamiento;
    }

    public record Comando(String nombre, String descripcion, String ciudad, String direccion, Ubicacion ubicacion,
                          List<String> normasConvivencia, List<ServicioAdicional> servicios,
                          Set<MedioPago> mediosPago, HorarioAlojamiento horario, UmbralEdadFacturable umbralEdad,
                          TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
                          HoraLimiteNoShow horaLimiteNoShow, Anticipo anticipo) {
    }
}

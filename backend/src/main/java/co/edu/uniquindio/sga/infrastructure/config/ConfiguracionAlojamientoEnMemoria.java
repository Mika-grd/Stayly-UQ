package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.model.alojamiento.Alojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.alojamiento.CargoPorMascota;
import co.edu.uniquindio.sga.domain.model.alojamiento.CupoMascotas;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.model.alojamiento.ServicioAdicional;
import co.edu.uniquindio.sga.domain.model.alojamiento.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.Ubicacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

/**
 * Implementa el puerto ConfiguracionAlojamiento. Al iniciar toma los valores de la Ficha de
 * application.properties (así un valor fuera de rango hace fallar el arranque con su
 * ReglaDominioException); después el administrador los cambia por la API (CU-36, CU-37) y cada
 * servicio lee el valor vigente en su siguiente uso. Un reinicio vuelve a los de application.properties.
 */
public class ConfiguracionAlojamientoEnMemoria implements ConfiguracionAlojamiento {

    private volatile Alojamiento alojamiento;

    public ConfiguracionAlojamientoEnMemoria(PropiedadesAlojamiento propiedades) {
        HorarioAlojamiento horario = new HorarioAlojamiento(LocalTime.parse(propiedades.horaEntrada()),
                LocalTime.parse(propiedades.horaSalida()));
        this.alojamiento = Alojamiento.crear(
                UUID.nameUUIDFromBytes(propiedades.nombre().getBytes(StandardCharsets.UTF_8)), propiedades.nombre(),
                propiedades.descripcion(), propiedades.ciudad(), propiedades.direccion(),
                new Ubicacion(propiedades.latitud(), propiedades.longitud()), propiedades.normasConvivencia(),
                propiedades.servicios().stream()
                        .map(servicio -> new ServicioAdicional(servicio.nombre(), servicio.generaCargo(),
                                Dinero.pesos(servicio.valor())))
                        .toList(),
                Set.copyOf(propiedades.mediosPago()), horario,
                new UmbralEdadFacturable(propiedades.umbralEdadFacturable()),
                new TiempoPreparacion(propiedades.tiempoPreparacionHoras()),
                new PlazoConfirmacion(propiedades.plazoConfirmacionHoras()),
                new HoraLimiteNoShow(LocalTime.parse(propiedades.horaLimiteNoShow())),
                new Anticipo(propiedades.anticipo().exigido(), propiedades.anticipo().porcentaje()),
                new CupoMascotas(propiedades.mascotas().cupoMaximo()),
                new CargoPorMascota(Dinero.pesos(propiedades.mascotas().cargoPorNoche())),
                new RecargoLlegadaNocturna(LocalTime.parse(propiedades.recargoLlegadaNocturna().horaInicio()),
                        Dinero.pesos(propiedades.recargoLlegadaNocturna().valor())));
    }

    @Override public UmbralEdadFacturable umbralEdad() { return alojamiento.umbralEdad(); }
    @Override public TiempoPreparacion tiempoPreparacion() { return alojamiento.tiempoPreparacion(); }
    @Override public HorarioAlojamiento horario() { return alojamiento.horario(); }
    @Override public PlazoConfirmacion plazoConfirmacion() { return alojamiento.plazoConfirmacion(); }
    @Override public HoraLimiteNoShow horaLimiteNoShow() { return alojamiento.horaLimiteNoShow(); }
    @Override public Anticipo anticipo() { return alojamiento.anticipo(); }
    @Override public CupoMascotas cupoMascotas() { return alojamiento.cupoMascotas(); }
    @Override public CargoPorMascota cargoPorMascota() { return alojamiento.cargoPorMascota(); }
    @Override public RecargoLlegadaNocturna recargoLlegadaNocturna() { return alojamiento.recargoLlegadaNocturna(); }
    @Override public Alojamiento alojamiento() { return alojamiento; }

    @Override
    public void guardar(Alojamiento alojamiento) {
        this.alojamiento = alojamiento;
    }
}

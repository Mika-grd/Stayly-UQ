package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.model.alojamiento.Anticipo;
import co.edu.uniquindio.sga.domain.model.alojamiento.CargoPorMascota;
import co.edu.uniquindio.sga.domain.model.alojamiento.CupoMascotas;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.model.alojamiento.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;

import java.time.LocalTime;

/**
 * Implementa el puerto ConfiguracionAlojamiento con los valores de la Ficha cargados al iniciar.
 * Construye los objetos de valor una sola vez, así un valor fuera de rango en application.yml
 * hace fallar el arranque con su ReglaDominioException.
 */
public class ConfiguracionAlojamientoEnMemoria implements ConfiguracionAlojamiento {

    private final UmbralEdadFacturable umbralEdad;
    private final TiempoPreparacion tiempoPreparacion;
    private final HorarioAlojamiento horario;
    private final PlazoConfirmacion plazoConfirmacion;
    private final HoraLimiteNoShow horaLimiteNoShow;
    private final Anticipo anticipo;
    private final CupoMascotas cupoMascotas;
    private final CargoPorMascota cargoPorMascota;
    private final RecargoLlegadaNocturna recargoLlegadaNocturna;

    public ConfiguracionAlojamientoEnMemoria(PropiedadesAlojamiento propiedades) {
        this.umbralEdad = new UmbralEdadFacturable(propiedades.umbralEdadFacturable());
        this.tiempoPreparacion = new TiempoPreparacion(propiedades.tiempoPreparacionHoras());
        this.horario = new HorarioAlojamiento(LocalTime.parse(propiedades.horaEntrada()),
                LocalTime.parse(propiedades.horaSalida()));
        this.plazoConfirmacion = new PlazoConfirmacion(propiedades.plazoConfirmacionHoras());
        this.horaLimiteNoShow = new HoraLimiteNoShow(LocalTime.parse(propiedades.horaLimiteNoShow()));
        this.anticipo = new Anticipo(propiedades.anticipo().exigido(), propiedades.anticipo().porcentaje());
        this.cupoMascotas = new CupoMascotas(propiedades.mascotas().cupoMaximo());
        this.cargoPorMascota = new CargoPorMascota(Dinero.pesos(propiedades.mascotas().cargoPorNoche()));
        this.recargoLlegadaNocturna = new RecargoLlegadaNocturna(
                LocalTime.parse(propiedades.recargoLlegadaNocturna().horaInicio()),
                Dinero.pesos(propiedades.recargoLlegadaNocturna().valor()));
    }

    @Override public UmbralEdadFacturable umbralEdad() { return umbralEdad; }
    @Override public TiempoPreparacion tiempoPreparacion() { return tiempoPreparacion; }
    @Override public HorarioAlojamiento horario() { return horario; }
    @Override public PlazoConfirmacion plazoConfirmacion() { return plazoConfirmacion; }
    @Override public HoraLimiteNoShow horaLimiteNoShow() { return horaLimiteNoShow; }
    @Override public Anticipo anticipo() { return anticipo; }
    @Override public CupoMascotas cupoMascotas() { return cupoMascotas; }
    @Override public CargoPorMascota cargoPorMascota() { return cargoPorMascota; }
    @Override public RecargoLlegadaNocturna recargoLlegadaNocturna() { return recargoLlegadaNocturna; }
}

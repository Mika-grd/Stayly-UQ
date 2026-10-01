package co.edu.uniquindio.sga.domain.soporte;

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

/** Fake del puerto con los valores de la Ficha de Stayly-UQ (sección 5 del enunciado). */
public class ConfiguracionAlojamientoFake implements ConfiguracionAlojamiento {

    private TiempoPreparacion tiempoPreparacion = new TiempoPreparacion(3);

    public static ConfiguracionAlojamientoFake ficha() {
        return new ConfiguracionAlojamientoFake();
    }

    /** Para probar RN-20 con un tiempo de preparación que no cabe en la ventana 12:00–15:00. */
    public ConfiguracionAlojamientoFake conTiempoPreparacion(int horas) {
        this.tiempoPreparacion = new TiempoPreparacion(horas);
        return this;
    }

    @Override public UmbralEdadFacturable umbralEdad() { return new UmbralEdadFacturable(12); }
    @Override public TiempoPreparacion tiempoPreparacion() { return tiempoPreparacion; }
    @Override public HorarioAlojamiento horario() { return new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(12, 0)); }
    @Override public PlazoConfirmacion plazoConfirmacion() { return new PlazoConfirmacion(24); }
    @Override public HoraLimiteNoShow horaLimiteNoShow() { return new HoraLimiteNoShow(LocalTime.of(23, 0)); }
    @Override public Anticipo anticipo() { return new Anticipo(true, 30); }
    @Override public CupoMascotas cupoMascotas() { return new CupoMascotas(2); }
    @Override public CargoPorMascota cargoPorMascota() { return new CargoPorMascota(Dinero.pesos(25_000)); }

    @Override
    public RecargoLlegadaNocturna recargoLlegadaNocturna() {
        return new RecargoLlegadaNocturna(LocalTime.of(21, 0), Dinero.pesos(30_000));
    }
}

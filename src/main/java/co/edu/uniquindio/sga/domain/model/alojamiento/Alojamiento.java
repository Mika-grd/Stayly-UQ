package co.edu.uniquindio.sga.domain.model.alojamiento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * El negocio completo (F-13: el sistema administra un único alojamiento) y su configuración
 * vigente. El dominio no lo lee directamente: lo consulta a través del puerto
 * {@code ConfiguracionAlojamiento}.
 */
public class Alojamiento {

    private final UUID id;
    private final String nombre;
    private final String descripcion;
    private final String ciudad;
    private final String direccion;
    private final Ubicacion ubicacion;
    private HorarioAlojamiento horario;
    private final List<String> normasConvivencia;
    private UmbralEdadFacturable umbralEdad;
    private TiempoPreparacion tiempoPreparacion;
    private PlazoConfirmacion plazoConfirmacion;
    private HoraLimiteNoShow horaLimiteNoShow;
    private Anticipo anticipo;
    private final List<ServicioAdicional> servicios;
    private final Set<MedioPago> mediosPago;
    private CupoMascotas cupoMascotas;
    private CargoPorMascota cargoPorMascota;
    private RecargoLlegadaNocturna recargoLlegadaNocturna;

    private Alojamiento(UUID id, String nombre, String descripcion, String ciudad, String direccion,
                        Ubicacion ubicacion, List<String> normasConvivencia, List<ServicioAdicional> servicios,
                        Set<MedioPago> mediosPago) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.ubicacion = ubicacion;
        this.normasConvivencia = List.copyOf(normasConvivencia);
        this.servicios = List.copyOf(servicios);
        this.mediosPago = Set.copyOf(mediosPago);
    }

    public static Alojamiento crear(UUID id, String nombre, String descripcion, String ciudad, String direccion,
                                    Ubicacion ubicacion, List<String> normasConvivencia,
                                    List<ServicioAdicional> servicios, Set<MedioPago> mediosPago,
                                    HorarioAlojamiento horario, UmbralEdadFacturable umbralEdad,
                                    TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
                                    HoraLimiteNoShow horaLimiteNoShow, Anticipo anticipo, CupoMascotas cupoMascotas,
                                    CargoPorMascota cargoPorMascota, RecargoLlegadaNocturna recargoLlegadaNocturna) {
        if (id == null || nombre == null || nombre.isBlank() || ubicacion == null
                || normasConvivencia == null || servicios == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El alojamiento requiere identidad, nombre y ubicación.");
        }
        if (mediosPago == null || mediosPago.size() < 2) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El alojamiento debe aceptar al menos dos medios de pago.");
        }
        Alojamiento alojamiento = new Alojamiento(id, nombre, descripcion, ciudad, direccion, ubicacion,
                normasConvivencia, servicios, mediosPago);
        alojamiento.actualizarParametros(horario, umbralEdad, tiempoPreparacion, plazoConfirmacion,
                horaLimiteNoShow, anticipo, cupoMascotas, cargoPorMascota, recargoLlegadaNocturna);
        return alojamiento;
    }

    /** Reemplaza los parámetros de la sección 6. Cada objeto de valor ya validó su rango (AL-2). */
    public void actualizarParametros(HorarioAlojamiento horario, UmbralEdadFacturable umbralEdad,
                                     TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
                                     HoraLimiteNoShow horaLimiteNoShow, Anticipo anticipo, CupoMascotas cupoMascotas,
                                     CargoPorMascota cargoPorMascota, RecargoLlegadaNocturna recargoLlegadaNocturna) {
        if (horario == null || umbralEdad == null || tiempoPreparacion == null || plazoConfirmacion == null
                || horaLimiteNoShow == null || anticipo == null || cupoMascotas == null || cargoPorMascota == null
                || recargoLlegadaNocturna == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Todos los parámetros del alojamiento son obligatorios.");
        }
        this.horario = horario;
        this.umbralEdad = umbralEdad;
        this.tiempoPreparacion = tiempoPreparacion;
        this.plazoConfirmacion = plazoConfirmacion;
        this.horaLimiteNoShow = horaLimiteNoShow;
        this.anticipo = anticipo;
        this.cupoMascotas = cupoMascotas;
        this.cargoPorMascota = cargoPorMascota;
        this.recargoLlegadaNocturna = recargoLlegadaNocturna;
    }

    public boolean aceptaMedioPago(MedioPago medio) {
        return mediosPago.contains(medio);
    }

    public UUID id() { return id; }
    public String nombre() { return nombre; }
    public String descripcion() { return descripcion; }
    public String ciudad() { return ciudad; }
    public String direccion() { return direccion; }
    public Ubicacion ubicacion() { return ubicacion; }
    public HorarioAlojamiento horario() { return horario; }
    public List<String> normasConvivencia() { return normasConvivencia; }
    public UmbralEdadFacturable umbralEdad() { return umbralEdad; }
    public TiempoPreparacion tiempoPreparacion() { return tiempoPreparacion; }
    public PlazoConfirmacion plazoConfirmacion() { return plazoConfirmacion; }
    public HoraLimiteNoShow horaLimiteNoShow() { return horaLimiteNoShow; }
    public Anticipo anticipo() { return anticipo; }
    public List<ServicioAdicional> servicios() { return servicios; }
    public Set<MedioPago> mediosPago() { return mediosPago; }
    public CupoMascotas cupoMascotas() { return cupoMascotas; }
    public CargoPorMascota cargoPorMascota() { return cargoPorMascota; }
    public RecargoLlegadaNocturna recargoLlegadaNocturna() { return recargoLlegadaNocturna; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Alojamiento otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

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
    private String nombre;
    private String descripcion;
    private String ciudad;
    private String direccion;
    private Ubicacion ubicacion;
    private HorarioAlojamiento horario;
    private List<String> normasConvivencia;
    private UmbralEdadFacturable umbralEdad;
    private TiempoPreparacion tiempoPreparacion;
    private PlazoConfirmacion plazoConfirmacion;
    private HoraLimiteNoShow horaLimiteNoShow;
    private Anticipo anticipo;
    private List<ServicioAdicional> servicios;
    private Set<MedioPago> mediosPago;
    private CupoMascotas cupoMascotas;
    private CargoPorMascota cargoPorMascota;
    private RecargoLlegadaNocturna recargoLlegadaNocturna;

    private Alojamiento(UUID id) {
        this.id = id;
    }

    public static Alojamiento crear(UUID id, String nombre, String descripcion, String ciudad, String direccion,
                                    Ubicacion ubicacion, List<String> normasConvivencia,
                                    List<ServicioAdicional> servicios, Set<MedioPago> mediosPago,
                                    HorarioAlojamiento horario, UmbralEdadFacturable umbralEdad,
                                    TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
                                    HoraLimiteNoShow horaLimiteNoShow, Anticipo anticipo, CupoMascotas cupoMascotas,
                                    CargoPorMascota cargoPorMascota, RecargoLlegadaNocturna recargoLlegadaNocturna) {
        if (id == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El alojamiento requiere identidad, nombre y ubicación.");
        }
        Alojamiento alojamiento = new Alojamiento(id);
        alojamiento.actualizarDatos(nombre, descripcion, ciudad, direccion, ubicacion, normasConvivencia, servicios,
                mediosPago);
        alojamiento.actualizarParametros(horario, umbralEdad, tiempoPreparacion, plazoConfirmacion,
                horaLimiteNoShow, anticipo, cupoMascotas, cargoPorMascota, recargoLlegadaNocturna);
        return alojamiento;
    }

    /**
     * Reemplaza la identidad del alojamiento (F-13): nombre, ubicación, normas, servicios y medios de pago.
     * Exige nombre, ubicación y al menos dos medios de pago (sección 6).
     */
    public void actualizarDatos(String nombre, String descripcion, String ciudad, String direccion,
                                Ubicacion ubicacion, List<String> normasConvivencia, List<ServicioAdicional> servicios,
                                Set<MedioPago> mediosPago) {
        if (nombre == null || nombre.isBlank() || ubicacion == null || normasConvivencia == null || servicios == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El alojamiento requiere identidad, nombre y ubicación.");
        }
        if (mediosPago == null || mediosPago.size() < 2) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El alojamiento debe aceptar al menos dos medios de pago.");
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.ubicacion = ubicacion;
        this.normasConvivencia = List.copyOf(normasConvivencia);
        this.servicios = List.copyOf(servicios);
        this.mediosPago = Set.copyOf(mediosPago);
    }

    /** Reemplaza solo los parámetros de las reglas propias RP-02 (mascotas) y RP-03 (llegada nocturna). */
    public void actualizarReglasPropias(CupoMascotas cupoMascotas, CargoPorMascota cargoPorMascota,
                                        RecargoLlegadaNocturna recargoLlegadaNocturna) {
        actualizarParametros(horario, umbralEdad, tiempoPreparacion, plazoConfirmacion, horaLimiteNoShow, anticipo,
                cupoMascotas, cargoPorMascota, recargoLlegadaNocturna);
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

package co.edu.uniquindio.sga.domain.model.apartamento;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Raíz del agregado Apartamento: la unidad vendible completa (F-01). Nace inactivo y en
 * PENDIENTE_PREPARACION; se activa cuando tiene tarifas completas. Invariantes A-1 a A-5.
 */
public class Apartamento {

    private static final int MAXIMO_IMAGENES = 10;

    private final IdentificacionApartamento identificacion;
    private String nombre;
    private String descripcion;
    private int dormitorios;
    private Capacidad capacidad;
    private boolean admiteMascotas;
    private String advertenciaAcceso;
    private Set<Caracteristica> caracteristicas;
    private List<ImagenApartamento> imagenes;
    private EstadoOperativo estadoOperativo;
    private boolean activo;

    private Apartamento(IdentificacionApartamento identificacion, String nombre, String descripcion, int dormitorios,
                        Capacidad capacidad, boolean admiteMascotas, String advertenciaAcceso,
                        Set<Caracteristica> caracteristicas, List<ImagenApartamento> imagenes) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.admiteMascotas = admiteMascotas;
        this.advertenciaAcceso = advertenciaAcceso;
        this.caracteristicas = Set.copyOf(caracteristicas);
        this.imagenes = new ArrayList<>(imagenes);
        this.estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
        this.activo = false;
    }

    /** A-4: entre 1 y 10 imágenes y exactamente una principal. */
    public static Apartamento crear(IdentificacionApartamento identificacion, String nombre, String descripcion,
                                    int dormitorios, Capacidad capacidad, boolean admiteMascotas,
                                    String advertenciaAcceso, Set<Caracteristica> caracteristicas,
                                    List<ImagenApartamento> imagenes) {
        if (identificacion == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "El apartamento requiere identificación, nombre y capacidad.");
        }
        validarDatos(nombre, dormitorios, capacidad, caracteristicas, imagenes);
        return new Apartamento(identificacion, nombre.trim(), descripcion, dormitorios, capacidad, admiteMascotas,
                normalizarAdvertencia(advertenciaAcceso), caracteristicas, imagenes);
    }

    /**
     * 7.3: reemplaza los datos descriptivos con las mismas validaciones de {@link #crear} (A-4). La
     * identificación nunca cambia (L-04). Bajar la capacidad no afecta las reservas ya creadas: el
     * caso de uso advierte cuáles quedan por encima con {@link #admite}.
     */
    public void actualizarDatos(String nombre, String descripcion, int dormitorios, Capacidad capacidad,
                                boolean admiteMascotas, String advertenciaAcceso, Set<Caracteristica> caracteristicas,
                                List<ImagenApartamento> imagenes) {
        validarDatos(nombre, dormitorios, capacidad, caracteristicas, imagenes);
        this.nombre = nombre.trim();
        this.descripcion = descripcion;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.admiteMascotas = admiteMascotas;
        this.advertenciaAcceso = normalizarAdvertencia(advertenciaAcceso);
        this.caracteristicas = Set.copyOf(caracteristicas);
        this.imagenes = new ArrayList<>(imagenes);
    }

    private static void validarDatos(String nombre, int dormitorios, Capacidad capacidad,
                                     Set<Caracteristica> caracteristicas, List<ImagenApartamento> imagenes) {
        if (nombre == null || nombre.isBlank() || capacidad == null || caracteristicas == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "El apartamento requiere identificación, nombre y capacidad.");
        }
        if (dormitorios <= 0) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El apartamento debe tener al menos un dormitorio.");
        }
        if (imagenes == null || imagenes.isEmpty() || imagenes.size() > MAXIMO_IMAGENES
                || imagenes.stream().anyMatch(Objects::isNull)
                || imagenes.stream().filter(ImagenApartamento::principal).count() != 1) {
            throw new ReglaDominioException("IMAGENES_INVALIDAS",
                    "El apartamento debe tener entre 1 y 10 imágenes y una principal.");
        }
    }

    private static String normalizarAdvertencia(String advertenciaAcceso) {
        return advertenciaAcceso == null || advertenciaAcceso.isBlank() ? null : advertenciaAcceso.trim();
    }

    /**
     * Rearma un apartamento ya existente con su estado operativo y su indicador de activo. Repite
     * las validaciones de forma de {@link #crear} (A-4: imágenes, dormitorios, capacidad).
     */
    public static Apartamento reconstituir(IdentificacionApartamento identificacion, String nombre,
                                           String descripcion, int dormitorios, Capacidad capacidad,
                                           boolean admiteMascotas, String advertenciaAcceso,
                                           Set<Caracteristica> caracteristicas, List<ImagenApartamento> imagenes,
                                           EstadoOperativo estadoOperativo, boolean activo) {
        if (estadoOperativo == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El apartamento guardado requiere su estado operativo.");
        }
        Apartamento apartamento = crear(identificacion, nombre, descripcion, dormitorios, capacidad, admiteMascotas,
                advertenciaAcceso, caracteristicas, imagenes);
        apartamento.estadoOperativo = estadoOperativo;
        apartamento.activo = activo;
        return apartamento;
    }

    /** RN-02: la capacidad es un tope rígido. */
    public boolean admite(int totalOcupantes) {
        return capacidad.admite(totalOcupantes);
    }

    public boolean admiteMascotas() {
        return admiteMascotas;
    }

    public boolean puedeRecibirGrupo() {
        return estadoOperativo.permiteRegistro();
    }

    /** A-2 / RN-11: nunca recibe un grupo si no está PREPARADO. */
    public void marcarOcupado() {
        if (!puedeRecibirGrupo()) {
            throw new ReglaDominioException("APARTAMENTO_NO_PREPARADO",
                    "El apartamento no está preparado para recibir al grupo.");
        }
        transicionarA(EstadoOperativo.OCUPADO);
    }

    /** Al registrar la salida queda pendiente de preparación. */
    public void liberar() {
        transicionarA(EstadoOperativo.PENDIENTE_PREPARACION);
    }

    public void iniciarPreparacion() {
        transicionarA(EstadoOperativo.EN_PREPARACION);
    }

    public void marcarPreparado() {
        transicionarA(EstadoOperativo.PREPARADO);
    }

    /** A-3: nunca estando OCUPADO. No bloquea fechas por sí solo (3.3). */
    public void declararFueraDeServicio() {
        if (estadoOperativo == EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException("APARTAMENTO_OCUPADO",
                    "No se puede declarar fuera de servicio un apartamento ocupado.");
        }
        transicionarA(EstadoOperativo.FUERA_DE_SERVICIO);
    }

    /** FUERA_DE_SERVICIO → PENDIENTE_PREPARACION, nunca directo a PREPARADO (7.6). */
    public void volverAServicio() {
        if (estadoOperativo != EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException("TRANSICION_ESTADO_OPERATIVO_INVALIDA",
                    "Solo un apartamento fuera de servicio puede volver a servicio.");
        }
        transicionarA(EstadoOperativo.PENDIENTE_PREPARACION);
    }

    public Optional<String> advertenciaAcceso() {
        return Optional.ofNullable(advertenciaAcceso);
    }

    /** Las tarifas completas (7.4) las verifica VerificadorTarifasCompletasService antes de activar. */
    public void activar() {
        activo = true;
    }

    /** Eliminación lógica (7.3); las reservas activas o futuras las verifica RetiroApartamentoService. */
    public void retirar() {
        activo = false;
    }

    /** No afecta las reservas ya creadas (7.3). */
    public void cambiarCapacidad(Capacidad nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La capacidad debe ser un entero mayor que cero.");
        }
        capacidad = nueva;
    }

    public void agregarImagen(ImagenApartamento imagen) {
        if (imagen == null || imagenes.size() >= MAXIMO_IMAGENES
                || (imagen.principal() && imagenes.stream().anyMatch(ImagenApartamento::principal))) {
            throw new ReglaDominioException("IMAGENES_INVALIDAS",
                    "El apartamento debe tener entre 1 y 10 imágenes y una principal.");
        }
        imagenes.add(imagen);
    }

    public boolean estaActivo() {
        return activo;
    }

    private void transicionarA(EstadoOperativo destino) {
        if (!estadoOperativo.puedeTransicionarA(destino)) {
            throw new ReglaDominioException("TRANSICION_ESTADO_OPERATIVO_INVALIDA",
                    "El apartamento no puede pasar de " + estadoOperativo + " a " + destino + ".");
        }
        estadoOperativo = destino;
    }

    public IdentificacionApartamento identificacion() { return identificacion; }
    public String nombre() { return nombre; }
    public String descripcion() { return descripcion; }
    public int dormitorios() { return dormitorios; }
    public Capacidad capacidad() { return capacidad; }
    public Set<Caracteristica> caracteristicas() { return caracteristicas; }
    public List<ImagenApartamento> imagenes() { return Collections.unmodifiableList(imagenes); }
    public EstadoOperativo estadoOperativo() { return estadoOperativo; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Apartamento otro && identificacion.equals(otro.identificacion));
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }
}

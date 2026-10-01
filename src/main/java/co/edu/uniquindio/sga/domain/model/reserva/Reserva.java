package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Reserva. Referencia al Apartamento y a la PoliticaCancelacion solo por
 * identificador. Invariantes R-1 a R-6 (hoja "Agregados e Invariantes").
 */
public class Reserva {

    private final CodigoReserva codigo;
    private IdentificacionApartamento apartamento;
    private Estancia estancia;
    private List<Ocupante> ocupantes;
    private EstadoReserva estado;
    private final CanalOrigen canal;
    private final IdentificadorExterno identificadorExterno;
    private HoraEstimadaLlegada horaEstimadaLlegada;
    private MascotasAutorizadas mascotas;
    private ValorCongelado valor;
    private final VersionPolitica versionPolitica;
    private final LocalDateTime fechaCreacion;
    private final List<EventoReserva> eventos = new ArrayList<>();

    private Reserva(CodigoReserva codigo, IdentificacionApartamento apartamento, Estancia estancia,
                    List<Ocupante> ocupantes, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                    HoraEstimadaLlegada horaEstimadaLlegada, MascotasAutorizadas mascotas, ValorCongelado valor,
                    VersionPolitica versionPolitica, LocalDateTime fechaCreacion) {
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = List.copyOf(ocupantes);
        this.estado = EstadoReserva.PENDIENTE;
        this.canal = canal;
        this.identificadorExterno = identificadorExterno;
        this.horaEstimadaLlegada = horaEstimadaLlegada;
        this.mascotas = mascotas;
        this.valor = valor;
        this.versionPolitica = versionPolitica;
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Crea la reserva en PENDIENTE. Protege RN-04 (R-2) y que el titular sea un ocupante
     * facturable a la fecha de entrada (R-6). El valor y la versión de política llegan ya
     * calculados y quedan congelados (RN-22).
     */
    public static Reserva crear(CodigoReserva codigo, IdentificacionApartamento apartamento, Estancia estancia,
                                List<Ocupante> ocupantes, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                                HoraEstimadaLlegada horaEstimadaLlegada, MascotasAutorizadas mascotas,
                                ValorCongelado valor, VersionPolitica versionPolitica,
                                UmbralEdadFacturable umbral, String autor, LocalDateTime ahora) {
        if (codigo == null || apartamento == null || estancia == null || canal == null || mascotas == null
                || valor == null || versionPolitica == null || umbral == null || ahora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Faltan datos obligatorios para crear la reserva.");
        }
        verificarEntradaNoPasada(estancia, ahora.toLocalDate());
        verificarTitular(ocupantes, estancia, umbral);
        if (canal == CanalOrigen.EXTERNO && identificadorExterno == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "Una reserva del canal externo requiere su identificador externo.");
        }
        Reserva reserva = new Reserva(codigo, apartamento, estancia, ocupantes, canal, identificadorExterno,
                horaEstimadaLlegada, mascotas, valor, versionPolitica, ahora);
        reserva.anotar("Reserva creada en estado PENDIENTE", autor, ahora);
        return reserva;
    }

    /** Registra o cambia la hora estimada de llegada; solo antes del registro (CU-14). */
    public void indicarHoraEstimadaLlegada(HoraEstimadaLlegada hora, String autor, LocalDateTime ahora) {
        if (hora == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La hora estimada de llegada es obligatoria.");
        }
        verificarModificable();
        this.horaEstimadaLlegada = hora;
        anotar("Hora estimada de llegada: " + hora.hora(), autor, ahora);
    }

    /** RN-08 y RN-09. El anticipo (L-11) lo verifica ConfirmacionReservaService antes de llamar aquí. */
    public void confirmar(String autor, LocalDateTime ahora) {
        verificarTransicion(EstadoReserva.CONFIRMADA);
        if (horaEstimadaLlegada == null) {
            throw new ReglaDominioException("HORA_LLEGADA_REQUERIDA",
                    "Debe registrar la hora estimada de llegada antes de confirmar.");
        }
        cambiarEstado(EstadoReserva.CONFIRMADA, autor, ahora);
    }

    /** RN-10: solo una reserva CONFIRMADA y desde su fecha de entrada. */
    public boolean puedeRegistrarLlegada(LocalDate hoy) {
        return estado == EstadoReserva.CONFIRMADA && !hoy.isBefore(estancia.entrada());
    }

    public void registrarLlegada(String autor, LocalDateTime ahora) {
        verificarTransicion(EstadoReserva.EN_CURSO);
        if (ahora.toLocalDate().isBefore(estancia.entrada())) {
            throw new ReglaDominioException("REGISTRO_ANTES_DE_ENTRADA",
                    "No se puede registrar la llegada antes de la fecha de entrada.");
        }
        cambiarEstado(EstadoReserva.EN_CURSO, autor, ahora);
    }

    /** La salida anticipada también pasa por aquí: no es cancelación (7.5). */
    public void registrarSalida(String autor, LocalDateTime ahora) {
        verificarTransicion(EstadoReserva.FINALIZADA);
        cambiarEstado(EstadoReserva.FINALIZADA, autor, ahora);
    }

    /** Solo antes del registro: EN_CURSO no puede pasar a CANCELADA (7.5). */
    public void cancelar(String autor, LocalDateTime ahora) {
        verificarTransicion(EstadoReserva.CANCELADA);
        cambiarEstado(EstadoReserva.CANCELADA, autor, ahora);
    }

    /** L-15: desde la hora límite del día de entrada. */
    public void declararNoShow(HoraLimiteNoShow limite, String autor, LocalDateTime ahora) {
        verificarTransicion(EstadoReserva.NO_SHOW);
        if (!limite.alcanzada(estancia.entrada(), ahora)) {
            throw new ReglaDominioException("NO_SHOW_ANTES_DE_HORA_LIMITE",
                    "Solo se puede declarar no-show desde la hora límite del día de entrada.");
        }
        cambiarEstado(EstadoReserva.NO_SHOW, autor, ahora);
    }

    /** RN-21: una PENDIENTE que superó el plazo de confirmación se cancela sola. */
    public void vencer(PlazoConfirmacion plazo, LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException("TRANSICION_INVALIDA",
                    "Solo vence una reserva PENDIENTE; esta está en estado " + estado + ".");
        }
        if (!plazo.vencido(fechaCreacion, ahora)) {
            throw new ReglaDominioException("TRANSICION_INVALIDA",
                    "La reserva PENDIENTE aún está dentro del plazo de confirmación.");
        }
        cambiarEstado(EstadoReserva.CANCELADA, "Sistema", ahora);
    }

    /**
     * RN-14: cambia apartamento, fechas, ocupantes o mascotas de una reserva PENDIENTE o
     * CONFIRMADA. Las validaciones que dependen de otros agregados (disponibilidad, tarifas,
     * estancia mínima, mascotas) ya las hizo el caso de uso con los servicios. La versión de
     * política no cambia. Devuelve la diferencia de valor que se registra como ajuste en el folio.
     */
    public Dinero modificar(IdentificacionApartamento nuevoApartamento, Estancia nuevaEstancia,
                            List<Ocupante> nuevosOcupantes, MascotasAutorizadas nuevasMascotas,
                            ValorCongelado nuevoValor, UmbralEdadFacturable umbral, String autor,
                            LocalDateTime ahora) {
        verificarModificable();
        if (nuevoApartamento == null || nuevaEstancia == null || nuevasMascotas == null || nuevoValor == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Faltan datos obligatorios para modificar la reserva.");
        }
        verificarEntradaNoPasada(nuevaEstancia, ahora.toLocalDate());
        verificarTitular(nuevosOcupantes, nuevaEstancia, umbral);
        Dinero diferencia = nuevoValor.diferenciaCon(valor);
        this.apartamento = nuevoApartamento;
        this.estancia = nuevaEstancia;
        this.ocupantes = List.copyOf(nuevosOcupantes);
        this.mascotas = nuevasMascotas;
        this.valor = nuevoValor;
        anotar("Reserva modificada; diferencia de valor " + diferencia, autor, ahora);
        return diferencia;
    }

    public boolean estaActiva() {
        return estado.retieneDisponibilidad();
    }

    public Ocupante titular() {
        return ocupantes.stream().filter(Ocupante::esTitular).findFirst().orElseThrow();
    }

    public ComposicionGrupo composicion() {
        return new ComposicionGrupo(ocupantes.stream().map(Ocupante::fechaNacimiento).toList());
    }

    public int totalOcupantes() {
        return ocupantes.size();
    }

    private void verificarTransicion(EstadoReserva destino) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new ReglaDominioException("TRANSICION_INVALIDA",
                    "La reserva en estado " + estado + " no puede pasar a " + destino + ".");
        }
    }

    private void verificarModificable() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("RESERVA_NO_MODIFICABLE",
                    "Solo se puede modificar una reserva pendiente o confirmada que no haya iniciado.");
        }
    }

    private static void verificarEntradaNoPasada(Estancia estancia, LocalDate hoy) {
        if (estancia.entrada().isBefore(hoy)) {
            throw new ReglaDominioException("FECHA_ENTRADA_PASADA",
                    "La fecha de entrada no puede ser anterior a la fecha actual.");
        }
    }

    private static void verificarTitular(List<Ocupante> ocupantes, Estancia estancia, UmbralEdadFacturable umbral) {
        if (ocupantes == null || ocupantes.isEmpty() || ocupantes.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La reserva debe tener al menos un ocupante.");
        }
        List<Ocupante> titulares = ocupantes.stream().filter(Ocupante::esTitular).toList();
        if (titulares.size() != 1) {
            throw new ReglaDominioException("TITULAR_NO_FACTURABLE",
                    "La reserva debe tener exactamente un titular.");
        }
        if (!titulares.get(0).esFacturableEn(estancia.entrada(), umbral)) {
            throw new ReglaDominioException("TITULAR_NO_FACTURABLE", "El titular debe ser un ocupante facturable.");
        }
    }

    private void cambiarEstado(EstadoReserva destino, String autor, LocalDateTime ahora) {
        EstadoReserva anterior = estado;
        estado = destino;
        anotar("Estado " + anterior + " → " + destino, autor, ahora);
    }

    private void anotar(String descripcion, String autor, LocalDateTime ahora) {
        eventos.add(new EventoReserva(descripcion, autor, ahora));
    }

    public CodigoReserva codigo() { return codigo; }
    public IdentificacionApartamento apartamento() { return apartamento; }
    public Estancia estancia() { return estancia; }
    public List<Ocupante> ocupantes() { return ocupantes; }
    public EstadoReserva estado() { return estado; }
    public CanalOrigen canal() { return canal; }
    public Optional<IdentificadorExterno> identificadorExterno() { return Optional.ofNullable(identificadorExterno); }
    public Optional<HoraEstimadaLlegada> horaEstimadaLlegada() { return Optional.ofNullable(horaEstimadaLlegada); }
    public MascotasAutorizadas mascotas() { return mascotas; }
    public ValorCongelado valor() { return valor; }
    public VersionPolitica versionPolitica() { return versionPolitica; }
    public LocalDateTime fechaCreacion() { return fechaCreacion; }
    public List<EventoReserva> eventos() { return Collections.unmodifiableList(eventos); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Reserva otra && codigo.equals(otra.codigo));
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.datos;

import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.IdNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.DatosContactoTitular;
import co.edu.uniquindio.sga.domain.model.reserva.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.IdOcupante;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.service.SalidaGrupoService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Datos de prueba de la sección 12.3, cargados solos al arrancar a partir de la Ficha del
 * Alojamiento (Anexo A). Todo se construye con los métodos del dominio y sus servicios, nunca con
 * SQL: cada reserva recorre su ciclo de vida real (anticipo, confirmación, llegada, salida) y su
 * valor lo calcula el cotizador con las tarifas de la Ficha. Las fechas son relativas a hoy, así
 * que los datos sirven el día de la sustentación. Se apaga con {@code sga.datos-prueba.habilitado=false}.
 */
@Component
@ConditionalOnProperty(name = "sga.datos-prueba.habilitado", havingValue = "true", matchIfMissing = true)
public class CargaDatosPrueba implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaDatosPrueba.class);

    /** Identificador fijo del simulador de canal del curso, para que la colección de pruebas lo conozca. */
    public static final IdCanal CANAL_SIMULADOR = new IdCanal(UUID.fromString("00000000-0000-0000-0000-0000000000e1"));

    private static final String RECEPCION = "recepcion@stayly-uq.co";
    private static final String ADMINISTRACION = "admin@stayly-uq.co";
    private static final LocalDateTime VIGENCIA_FICHA = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final ApartamentoRepository apartamentos;
    private final TemporadaRepository temporadas;
    private final TarifarioRepository tarifarios;
    private final PoliticaCancelacionRepository politicas;
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final BloqueoRepository bloqueos;
    private final NovedadRepository novedades;
    private final CanalRepository canales;
    private final ConfiguracionAlojamiento configuracion;
    private final CotizadorEstanciaService cotizador;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final ConfirmacionReservaService confirmacion;
    private final EntregaApartamentoService entrega;
    private final CalculadorRetencionService calculadorRetencion;
    private final AdmisionMascotasService admisionMascotas;
    private final SalidaGrupoService salida = new SalidaGrupoService();
    private final Clock reloj;

    public CargaDatosPrueba(ApartamentoRepository apartamentos, TemporadaRepository temporadas,
                            TarifarioRepository tarifarios, PoliticaCancelacionRepository politicas,
                            ReservaRepository reservas, FolioRepository folios, BloqueoRepository bloqueos,
                            NovedadRepository novedades, CanalRepository canales,
                            ConfiguracionAlojamiento configuracion, CotizadorEstanciaService cotizador,
                            VerificadorTarifasCompletasService verificadorTarifas,
                            ConfirmacionReservaService confirmacion, EntregaApartamentoService entrega,
                            CalculadorRetencionService calculadorRetencion, AdmisionMascotasService admisionMascotas,
                            Clock reloj) {
        this.apartamentos = apartamentos;
        this.temporadas = temporadas;
        this.tarifarios = tarifarios;
        this.politicas = politicas;
        this.reservas = reservas;
        this.folios = folios;
        this.bloqueos = bloqueos;
        this.novedades = novedades;
        this.canales = canales;
        this.configuracion = configuracion;
        this.cotizador = cotizador;
        this.verificadorTarifas = verificadorTarifas;
        this.confirmacion = confirmacion;
        this.entrega = entrega;
        this.calculadorRetencion = calculadorRetencion;
        this.admisionMascotas = admisionMascotas;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        if (apartamentos.buscarPorIdentificacion(new IdentificacionApartamento("SUQ-101")).isPresent()) {
            LOG.info("Datos de prueba ya cargados; no se vuelven a cargar.");
            return;
        }
        LocalDate hoy = LocalDate.now(reloj);
        cargarTemporadasYTarifas();
        cargarApartamentos();
        cargarPoliticaYCanal();
        cargarReservas(hoy);
        cargarFueraDeServicio(hoy);
        LOG.info("Datos de prueba cargados (12.3): 6 apartamentos, 3 temporadas, reservas en los 6 estados.");
    }

    // A.3 Temporadas y tarifas

    private void cargarTemporadasYTarifas() {
        IdTemporada base = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000b"));
        IdTemporada alta = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        IdTemporada media = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000c"));
        temporadas.guardar(Temporada.crearBase(base, "Base"));
        temporadas.guardar(Temporada.crear(alta, "Temporada alta", List.of(
                new RangoFechas(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16)),
                new RangoFechas(LocalDate.of(2027, 3, 21), LocalDate.of(2027, 3, 29))), new EstanciaMinima(2)));
        temporadas.guardar(Temporada.crear(media, "Temporada media", List.of(
                new RangoFechas(LocalDate.of(2027, 6, 15), LocalDate.of(2027, 7, 16))), new EstanciaMinima(1)));

        tarifario("SUQ-101", base, 95_000, alta, 128_000, media, 109_000);
        tarifario("SUQ-102", base, 90_000, alta, 122_000, media, 103_000);
        tarifario("SUQ-201", base, 75_000, alta, 101_000, media, 86_000);
        tarifario("SUQ-202", base, 78_000, alta, 105_000, media, 90_000);
        tarifario("SUQ-301", base, 65_000, alta, 88_000, media, 75_000);
        tarifario("SUQ-302", base, 90_000, alta, 130_000, media, 110_000);
    }

    private void tarifario(String apartamento, IdTemporada base, long valorBase, IdTemporada alta, long valorAlta,
                           IdTemporada media, long valorMedia) {
        Tarifario tarifario = Tarifario.nuevo(new IdentificacionApartamento(apartamento));
        tarifario.definirTarifa(base, Dinero.pesos(valorBase), VIGENCIA_FICHA);
        tarifario.definirTarifa(alta, Dinero.pesos(valorAlta), VIGENCIA_FICHA);
        tarifario.definirTarifa(media, Dinero.pesos(valorMedia), VIGENCIA_FICHA);
        tarifarios.guardar(tarifario);
    }

    // A.2 Inventario

    private void cargarApartamentos() {
        apartamento("SUQ-101", "Apartamento 101", "Un dormitorio con balcón y vista a la ciudad.", 1, 2, false, null,
                Set.of("Balcón con vista a la ciudad", "Cama doble", "Cocineta", "Wifi"));
        apartamento("SUQ-102", "Apartamento 102", "Un dormitorio en el primer piso, sin escaleras.", 1, 2, true, null,
                Set.of("Acceso para movilidad reducida", "Cocineta", "Patio interior", "Admite mascotas"));
        apartamento("SUQ-201", "Apartamento 201", "Dos dormitorios con espacio para trabajo remoto.", 2, 4, false,
                null, Set.of("Cocina equipada", "Escritorio para trabajo remoto", "Wifi de alta velocidad"));
        apartamento("SUQ-202", "Apartamento 202", "Dos dormitorios con terraza privada.", 2, 4, true, null,
                Set.of("Terraza privada con hamaca", "Cocina equipada", "Admite mascotas"));
        apartamento("SUQ-301", "Apartamento 301", "Tres dormitorios para grupos grandes.", 3, 6, true,
                "Tercer piso sin ascensor", Set.of("Dos baños", "Lavadora", "Cocina equipada", "Sala amplia",
                        "Admite mascotas"));
        apartamento("SUQ-302", "Apartamento 302", "Tres dormitorios con vista a la cordillera.", 3, 6, false,
                "Tercer piso sin ascensor", Set.of("Vista panorámica a la cordillera", "Dos baños"));
    }

    /** 7.4: solo se activa si tiene tarifa en todas las temporadas. Queda PREPARADO para recibir grupos. */
    private void apartamento(String id, String nombre, String descripcion, int dormitorios, int capacidad,
                             boolean admiteMascotas, String advertencia, Set<String> caracteristicas) {
        IdentificacionApartamento identificacion = new IdentificacionApartamento(id);
        Apartamento apartamento = Apartamento.crear(identificacion, nombre, descripcion, dormitorios,
                new Capacidad(capacidad), admiteMascotas, advertencia,
                Set.copyOf(caracteristicas.stream().map(Caracteristica::new).toList()),
                List.of(new ImagenApartamento("https://stayly-uq.co/img/" + id.toLowerCase() + "-1.jpg", true),
                        new ImagenApartamento("https://stayly-uq.co/img/" + id.toLowerCase() + "-2.jpg", false)));
        verificadorTarifas.verificarTarifasCompletas(identificacion);
        apartamento.activar();
        apartamento.iniciarPreparacion();
        apartamento.marcarPreparado();
        apartamentos.guardar(apartamento);
    }

    // A.4 Política de cancelación y A.5 canal externo

    private void cargarPoliticaYCanal() {
        politicas.guardar(PoliticaCancelacion.crearVersion(new VersionPolitica(1), List.of(
                new TramoPolitica(15, 0), new TramoPolitica(7, 30), new TramoPolitica(0, 60)), 30, VIGENCIA_FICHA));
        canales.guardar(Canal.registrar(CANAL_SIMULADOR, "Simulador de canal del curso", CanalOrigen.EXTERNO));
    }

    // 12.3: reservas en todos los estados y por los tres canales

    private void cargarReservas(LocalDate hoy) {
        LocalDateTime ahora = LocalDateTime.now(reloj);

        // PENDIENTE · portal: creada hoy, aún sin anticipo ni hora de llegada.
        nueva("SUQ-101", new Estancia(hoy.plusDays(20), hoy.plusDays(23)), CanalOrigen.PORTAL, null,
                List.of(titular("Laura Gómez", 1990, "1094000001", "laura.gomez@correo.co"),
                        acompanante("Andrés Ríos", 1988)), null, 0, ahora);

        // CONFIRMADA · directo: anticipo del 30 % pagado y hora estimada de llegada.
        Reservada confirmada = nueva("SUQ-201", new Estancia(hoy.plusDays(10), hoy.plusDays(13)), CanalOrigen.DIRECTO,
                null, List.of(titular("Carlos Mejía", 1985, "1094000002", "carlos.mejia@correo.co"),
                        acompanante("Paula Mejía", 1987), acompanante("Tomás Mejía", 2020)),
                LocalTime.of(16, 0), 0, ahora.minusDays(1));
        confirmar(confirmada, ahora.minusDays(1).plusHours(2));

        // EN_CURSO · externo: llegó ayer con una mascota (RP-02).
        Canal canal = canales.buscarPorId(CANAL_SIMULADOR).orElseThrow();
        IdentificadorExterno externo = new IdentificadorExterno(CANAL_SIMULADOR, "SIM-0001");
        LocalDateTime creadaExterna = ahora.minusDays(3);
        Reservada enCurso = nueva("SUQ-202", new Estancia(hoy.minusDays(1), hoy.plusDays(2)), CanalOrigen.EXTERNO,
                externo, List.of(titular("Sofía Herrera", 1992, "1094000003", "sofia.herrera@correo.co"),
                        acompanante("Mateo Herrera", 1991)), LocalTime.of(18, 0), 1, creadaExterna);
        canal.registrarEvento(new EventoCanal("RESERVA_RECIBIDA", externo.codigo(), creadaExterna, "ACEPTADA"));
        canales.guardar(canal);
        confirmar(enCurso, creadaExterna.plusHours(1));
        registrarLlegada(enCurso, hoy.minusDays(1).atTime(18, 10));

        // FINALIZADA · directo: pagó todo, cerró el folio y salió hace 3 días; el apartamento quedó por preparar.
        Reservada finalizada = nueva("SUQ-301", new Estancia(hoy.minusDays(6), hoy.minusDays(3)), CanalOrigen.DIRECTO,
                null, List.of(titular("Juliana Ortiz", 1980, "1094000004", "juliana.ortiz@correo.co"),
                        acompanante("Felipe Ortiz", 1979), acompanante("Valentina Ortiz", 2010),
                        acompanante("Samuel Ortiz", 2016)), LocalTime.of(15, 30), 0, ahora.minusDays(15));
        confirmar(finalizada, ahora.minusDays(15).plusHours(3));
        registrarLlegada(finalizada, hoy.minusDays(6).atTime(15, 40));
        pagarSaldo(finalizada, MedioPago.TARJETA, hoy.minusDays(3).atTime(11, 0));
        finalizada.folio().cerrar(hoy.minusDays(3).atTime(11, 5));
        Apartamento suq301 = apartamento(finalizada.reserva());
        salida.registrarSalida(finalizada.reserva(), finalizada.folio(), suq301, RECEPCION,
                hoy.minusDays(3).atTime(configuracion.horario().horaSalida()));
        guardar(finalizada);
        apartamentos.guardar(suq301);

        // CANCELADA · portal: cancelada con más de 15 días de antelación, sin retención.
        Reservada cancelada = nueva("SUQ-102", new Estancia(hoy.plusDays(30), hoy.plusDays(32)), CanalOrigen.PORTAL,
                null, List.of(titular("Daniel Castro", 1995, "1094000005", "daniel.castro@correo.co")),
                null, 0, ahora.minusDays(2));
        cancelada.folio().liquidarCancelacion(calculadorRetencion.calcularRetencion(cancelada.reserva(), ahora), ahora);
        cancelada.reserva().cancelar(cancelada.reserva().titular().contacto().orElseThrow().correo(), ahora);
        guardar(cancelada);

        // NO_SHOW · directo: no llegó antes de las 23:00 del día de entrada; se retiene el 30 % (el anticipo).
        Reservada noShow = nueva("SUQ-102", new Estancia(hoy.minusDays(2), hoy), CanalOrigen.DIRECTO, null,
                List.of(titular("Marcela Ruiz", 1989, "1094000006", "marcela.ruiz@correo.co"),
                        acompanante("Jorge Ruiz", 1986)), LocalTime.of(20, 0), 0, ahora.minusDays(10));
        confirmar(noShow, ahora.minusDays(10).plusHours(4));
        LocalDateTime trasHoraLimite = hoy.minusDays(2).atTime(configuracion.horaLimiteNoShow().hora()).plusMinutes(30);
        noShow.reserva().declararNoShow(configuracion.horaLimiteNoShow(), RECEPCION, trasHoraLimite);
        noShow.folio().liquidarCancelacion(calculadorRetencion.calcularPenalidadNoShow(noShow.reserva()),
                trasHoraLimite);
        guardar(noShow);
    }

    /** Crea la reserva PENDIENTE con su folio, como CU-02: el valor sale del cotizador y queda congelado. */
    private Reservada nueva(String apartamento, Estancia estancia, CanalOrigen canal, IdentificadorExterno externo,
                            List<Ocupante> ocupantes, LocalTime horaLlegada, int cantidadMascotas,
                            LocalDateTime creadaEn) {
        IdentificacionApartamento identificacion = new IdentificacionApartamento(apartamento);
        Apartamento destino = apartamentos.buscarPorIdentificacion(identificacion).orElseThrow();
        MascotasAutorizadas mascotas = new MascotasAutorizadas(cantidadMascotas);
        admisionMascotas.verificarMascotas(destino, mascotas);
        ComposicionGrupo grupo = new ComposicionGrupo(ocupantes.stream().map(Ocupante::fechaNacimiento).toList());
        ValorCongelado valor = cotizador.cotizar(identificacion, estancia, grupo);
        UmbralEdadFacturable umbral = configuracion.umbralEdad();
        String autor = canal == CanalOrigen.DIRECTO ? RECEPCION
                : canal == CanalOrigen.EXTERNO ? "canal-externo" : ocupantes.get(0).contacto().orElseThrow().correo();

        Reserva reserva = Reserva.crear(reservas.siguienteCodigo(), identificacion, estancia, ocupantes, canal, externo,
                horaLlegada == null ? null : new HoraEstimadaLlegada(horaLlegada), mascotas, valor,
                politicas.buscarVigente().version(), umbral, autor, creadaEn);
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva.codigo(), valor.total(), creadaEn);
        if (mascotas.hayMascotas()) {
            folio.registrarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL, "Cargo por mascota",
                    admisionMascotas.cargoPorMascotas(mascotas, estancia), creadaEn));
        }
        Reservada reservada = new Reservada(reserva, folio);
        guardar(reservada);
        return reservada;
    }

    /** CU-03: registra el anticipo exigido (L-11) y confirma con ConfirmacionReservaService. */
    private void confirmar(Reservada reservada, LocalDateTime momento) {
        Dinero anticipo = configuracion.anticipo().exigidoSobre(reservada.reserva().valor().total());
        if (anticipo.esPositivo()) {
            reservada.folio().registrarPago(new Pago(anticipo, MedioPago.TRANSFERENCIA, momento,
                    "Anticipo " + reservada.reserva().codigo()));
        }
        confirmacion.confirmar(reservada.reserva(), reservada.folio(), RECEPCION, momento);
        guardar(reservada);
    }

    /** CU-05: el apartamento debe estar PREPARADO (RN-11) y pasa a OCUPADO. */
    private void registrarLlegada(Reservada reservada, LocalDateTime momento) {
        Apartamento apartamento = apartamento(reservada.reserva());
        entrega.registrarLlegada(reservada.reserva(), apartamento, RECEPCION, momento);
        guardar(reservada);
        apartamentos.guardar(apartamento);
    }

    private void pagarSaldo(Reservada reservada, MedioPago medio, LocalDateTime momento) {
        Dinero saldo = reservada.folio().saldo().valor();
        if (saldo.esPositivo()) {
            reservada.folio().registrarPago(new Pago(saldo, medio, momento, "Pago del saldo"));
        }
    }

    // 12.3: un apartamento fuera de servicio con su bloqueo vigente (3.3)

    private void cargarFueraDeServicio(LocalDate hoy) {
        IdentificacionApartamento suq302 = new IdentificacionApartamento("SUQ-302");
        LocalDateTime reporte = hoy.minusDays(1).atTime(9, 0);
        novedades.guardar(Novedad.registrar(new IdNovedad(UUID.randomUUID()), suq302, "servicio@stayly-uq.co",
                "Fuga en la ducha del baño principal; se cerró el registro de agua.", GravedadNovedad.ALTA, reporte));
        Apartamento apartamento = apartamentos.buscarPorIdentificacion(suq302).orElseThrow();
        apartamento.declararFueraDeServicio();
        apartamentos.guardar(apartamento);
        bloqueos.guardar(Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), suq302,
                new RangoFechas(hoy, hoy.plusDays(10)), "Reparación de la ducha del baño principal", ADMINISTRACION,
                reporte.plusHours(1)));
    }

    private Apartamento apartamento(Reserva reserva) {
        return apartamentos.buscarPorIdentificacion(reserva.apartamento()).orElseThrow();
    }

    private void guardar(Reservada reservada) {
        reservas.guardar(reservada.reserva());
        folios.guardar(reservada.folio());
    }

    private static Ocupante titular(String nombre, int anioNacimiento, String documento, String correo) {
        return Ocupante.titular(new IdOcupante(UUID.randomUUID()), nombre, LocalDate.of(anioNacimiento, 3, 14),
                new DocumentoIdentidad(documento), new DatosContactoTitular(correo, "300" + documento.substring(3)));
    }

    private static Ocupante acompanante(String nombre, int anioNacimiento) {
        return Ocupante.acompanante(new IdOcupante(UUID.randomUUID()), nombre, LocalDate.of(anioNacimiento, 8, 2),
                null);
    }

    /** Una reserva y su folio, que se guardan juntos en la carga. */
    private record Reservada(Reserva reserva, Folio folio) {
    }
}

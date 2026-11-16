package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase.Criterios;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase.EstadoNoche;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase.NocheOcupacion;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Cotizacion;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CalculadorRetencionService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorEstanciaMinimaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.PoliticaCancelacionRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 7.5 de punta a punta: cotización, búsqueda, modificación, hora de llegada, no-show, vencimiento
 * y consultas, con repositorios en memoria y un reloj que avanza cuando la prueba lo necesita.
 */
class DisponibilidadYReservasUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final LocalDate VEINTE_NOV = LocalDate.of(2026, 11, 20);
    private static final LocalDate VEINTIDOS_NOV = LocalDate.of(2026, 11, 22);

    private final ConfiguracionAlojamientoFake configuracion = ConfiguracionAlojamientoFake.ficha();
    private final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    private final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    private final TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
    private final TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
    private final BloqueoRepositoryEnMemoria bloqueos = new BloqueoRepositoryEnMemoria();
    private final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria();
    private final VerificadorTarifasCompletasService verificadorTarifas =
            new VerificadorTarifasCompletasService(temporadas, tarifarios);
    private final CotizadorEstanciaService cotizador = new CotizadorEstanciaService(temporadas, tarifarios,
            configuracion);
    private final AdmisionMascotasService admisionMascotas = new AdmisionMascotasService(configuracion);
    private Clock reloj = relojEn(FichaStaylyUq.HOY);
    private ReservaRepositoryEnMemoria reservas;
    private Apartamento suq201;
    private Apartamento suq301;

    @BeforeEach
    void cargarFicha() {
        reservas = new ReservaRepositoryEnMemoria(reloj);
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        temporadas.guardar(FichaStaylyUq.temporadaMedia());
        tarifarios.guardar(FichaStaylyUq.tarifarioSuq201());
        tarifarios.guardar(FichaStaylyUq.tarifario("SUQ-301", 65_000, 88_000, 75_000));
        politicas.guardar(FichaStaylyUq.politicaV1());
        suq201 = FichaStaylyUq.suq201();
        suq301 = FichaStaylyUq.suq301();
        apartamentos.guardar(suq201);
        apartamentos.guardar(suq301);
    }

    // CU-01 Cotizar

    @Test
    @DisplayName("CU-01 · Cotiza noche por noche con los facturables y suma el cargo por mascotas (RN-05, RP-02)")
    void cotizarConMascota() {
        // Act: SUQ-301, 2 noches base, dos adultos y una niña de 13 años, una mascota
        Cotizacion cotizacion = cotizar().ejecutar(suq301.identificacion(), VEINTE_NOV, VEINTIDOS_NOV, grupoDeTres(),
                new MascotasAutorizadas(1));

        // Assert
        assertThat(cotizacion.valor().noches()).hasSize(2);
        assertThat(cotizacion.ocupantesTotales()).isEqualTo(3);
        assertThat(cotizacion.ocupantesFacturables()).isEqualTo(3);
        assertThat(cotizacion.valor().total()).as("2 × 3 × $65.000").isEqualTo(Dinero.pesos(390_000));
        assertThat(cotizacion.cargoMascotas()).as("1 × 2 × $25.000").isEqualTo(Dinero.pesos(50_000));
        assertThat(cotizacion.total()).isEqualTo(Dinero.pesos(440_000));
    }

    @Test
    @DisplayName("CU-01 · No se cotiza con mascotas un apartamento que no las admite (RP-02)")
    void noCotizarMascotasDondeNoSeAdmiten() {
        assertThatThrownBy(() -> cotizar().ejecutar(suq201.identificacion(), VEINTE_NOV, VEINTIDOS_NOV, grupoDeTres(),
                new MascotasAutorizadas(1)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("MASCOTAS_NO_ADMITIDAS");
    }

    // CU-20 Buscar disponibles

    @Test
    @DisplayName("CU-20 · Solo aparecen los apartamentos sin reservas que solapen, con su valor estimado (RN-01)")
    void buscarExcluyeOcupados() {
        // Arrange: SUQ-301 queda reservado del 20 al 22
        crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0));

        // Act
        Pagina<ApartamentoDisponible> pagina = buscar().ejecutar(new Criterios(LocalDate.of(2026, 11, 21),
                LocalDate.of(2026, 11, 23), grupoDeTres(), null, null, null), PeticionPagina.primera());

        // Assert
        assertThat(pagina.contenido()).extracting(ApartamentoDisponible::apartamento).containsExactly(suq201);
        assertThat(pagina.contenido().get(0).valorEstimado().total()).as("2 noches × 3 × $75.000")
                .isEqualTo(Dinero.pesos(450_000));
        assertThat(pagina.totalElementos()).isEqualTo(1);
        assertThat(pagina.tamano()).isEqualTo(10);
    }

    @Test
    @DisplayName("CU-20 · Filtra por capacidad mínima, características y precio máximo")
    void buscarConFiltros() {
        // Act + Assert
        assertThat(nombresDisponibles(new Criterios(VEINTE_NOV, VEINTIDOS_NOV, grupoDeTres(), 5, null, null)))
                .containsExactly("SUQ-301");
        assertThat(nombresDisponibles(new Criterios(VEINTE_NOV, VEINTIDOS_NOV, grupoDeTres(), null, null,
                Set.of(new Caracteristica("Cocina equipada"))))).containsExactly("SUQ-201");
        assertThat(nombresDisponibles(new Criterios(VEINTE_NOV, VEINTIDOS_NOV, grupoDeTres(), null,
                Dinero.pesos(400_000), null))).as("SUQ-201 cuesta $450.000").containsExactly("SUQ-301");
    }

    @Test
    @DisplayName("CU-20 · No se busca con entrada anterior a hoy ni con más ocupantes que la capacidad (RN-04, RN-02)")
    void buscarRechazaPasadoYExcluyeCapacidad() {
        assertThatThrownBy(() -> buscar().ejecutar(new Criterios(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3),
                grupoDeTres(), null, null, null), PeticionPagina.primera()))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("FECHA_ENTRADA_PASADA");
        List<LocalDate> grupoDeCinco = List.of(LocalDate.of(1990, 1, 1), LocalDate.of(1991, 1, 1),
                LocalDate.of(1992, 1, 1), LocalDate.of(1993, 1, 1), LocalDate.of(1994, 1, 1));
        assertThat(nombresDisponibles(new Criterios(VEINTE_NOV, VEINTIDOS_NOV, grupoDeCinco, null, null, null)))
                .as("SUQ-201 solo admite 4").containsExactly("SUQ-301");
    }

    // CU-13 Modificar

    @Test
    @DisplayName("CU-13 · Alargar la estancia recalcula el valor y deja la diferencia como ajuste; la política no cambia")
    void modificarRegistraAjuste() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();

        // Act: del 20 al 23 (una noche más)
        ModificarReservaUseCase.ReservaModificada modificada = modificar().ejecutar(
                comandoModificar(reserva.codigo(), LocalDate.of(2026, 11, 23), 0));

        // Assert
        assertThat(modificada.diferencia()).as("1 noche × 3 × $65.000").isEqualTo(Dinero.pesos(195_000));
        assertThat(modificada.reserva().valor().total()).isEqualTo(Dinero.pesos(585_000));
        assertThat(modificada.reserva().versionPolitica()).isEqualTo(new VersionPolitica(1));
        assertThat(modificada.folio().cargos()).extracting(c -> c.tipo())
                .containsExactly(TipoCargo.ALOJAMIENTO, TipoCargo.AJUSTE);
    }

    @Test
    @DisplayName("CU-13 · Quitar la mascota registra el movimiento inverso de su cargo (RP-02, RN-16)")
    void modificarQuitaMascota() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 1)).reserva();

        // Act
        Folio folio = modificar().ejecutar(comandoModificar(reserva.codigo(), VEINTIDOS_NOV, 0)).folio();

        // Assert
        assertThat(folio.cargos()).extracting(c -> c.valor())
                .containsExactly(Dinero.pesos(390_000), Dinero.pesos(50_000), Dinero.pesos(-50_000));
        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(390_000));
    }

    @Test
    @DisplayName("CU-13 · No se modifica hacia noches de otra reserva ni una reserva cancelada (RN-01, RN-14)")
    void modificarRevalida() {
        // Arrange
        Reserva primera = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        Reserva segunda = crearReserva().ejecutar(new CrearReservaUseCase.Comando(suq301.identificacion(),
                LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 27), ocupantes(), CanalOrigen.DIRECTO, null,
                LocalTime.of(16, 0), 0, "recepcion")).reserva();

        // Act + Assert
        assertThatThrownBy(() -> modificar().ejecutar(comandoModificar(primera.codigo(), LocalDate.of(2026, 11, 26), 0)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("NOCHES_NO_DISPONIBLES");
        segunda.cancelar("recepcion", FichaStaylyUq.HOY);
        assertThatThrownBy(() -> modificar().ejecutar(new ModificarReservaUseCase.Comando(segunda.codigo(),
                suq301.identificacion(), LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 30), ocupantes(), 0,
                "recepcion")))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("RESERVA_NO_MODIFICABLE");
    }

    // CU-14 Hora estimada de llegada

    @Test
    @DisplayName("CU-14 · Volverse nocturna registra el recargo y dejar de serlo lo revierte (RP-03)")
    void horaLlegadaConRecargoNocturno() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        IndicarHoraEstimadaLlegadaUseCase indicar = new IndicarHoraEstimadaLlegadaUseCase(reservas, folios,
                configuracion, reloj);

        // Act
        Folio nocturna = indicar.ejecutar(reserva.codigo(), new HoraEstimadaLlegada(LocalTime.of(22, 0)), "huesped")
                .folio();
        Dinero saldoNocturno = nocturna.saldo().valor();
        Folio diurna = indicar.ejecutar(reserva.codigo(), new HoraEstimadaLlegada(LocalTime.of(18, 0)), "huesped")
                .folio();

        // Assert
        assertThat(saldoNocturno).isEqualTo(Dinero.pesos(420_000));
        assertThat(diurna.saldo().valor()).isEqualTo(Dinero.pesos(390_000));
        assertThat(reservas.buscarPorCodigo(reserva.codigo()).orElseThrow().horaEstimadaLlegada())
                .contains(new HoraEstimadaLlegada(LocalTime.of(18, 0)));
    }

    // CU-12 No-show

    @Test
    @DisplayName("CU-12 · Después de las 23:00 del día de entrada pasa a NO_SHOW y retiene el 30 % (L-15, RN-13)")
    void declararNoShow() {
        // Arrange: confirmada con anticipo
        Reserva reserva = reservaConfirmada();

        // Act + Assert: antes de la hora límite se rechaza
        reloj = relojEn(LocalDateTime.of(2026, 11, 20, 22, 0));
        assertThatThrownBy(() -> noShow().ejecutar(reserva.codigo(), "recepcion"))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("NO_SHOW_ANTES_DE_HORA_LIMITE");
        reloj = relojEn(LocalDateTime.of(2026, 11, 20, 23, 30));
        Folio folio = noShow().ejecutar(reserva.codigo(), "recepcion");

        // Assert
        assertThat(reservas.buscarPorCodigo(reserva.codigo()).orElseThrow().estado()).isEqualTo(EstadoReserva.NO_SHOW);
        assertThat(folio.cargos()).extracting(c -> c.tipo()).contains(TipoCargo.PENALIDAD);
        assertThat(folio.saldo().esCero()).as("la penalidad del 30 % equivale al anticipo pagado").isTrue();
    }

    // CU-17 Vencimiento

    @Test
    @DisplayName("CU-17 · Una PENDIENTE pasa a CANCELADA al superar las 24 horas, no antes (RN-21)")
    void vencerPendientes() {
        // Arrange: creada el 01/10 a las 10:00
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();

        // Act + Assert
        reloj = relojEn(FichaStaylyUq.HOY.plusHours(23));
        assertThat(vencer().ejecutar()).isEmpty();
        reloj = relojEn(FichaStaylyUq.HOY.plusHours(25));
        assertThat(vencer().ejecutar()).containsExactly(reserva.codigo());

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(folios.buscarPorCodigoReserva(reserva.codigo()).orElseThrow().saldo().esCero())
                .as("sin retención: nunca quedó en firme").isTrue();
    }

    // Consultas

    @Test
    @DisplayName("7.5 · El listado filtra por estado y titular y va de la más reciente a la más antigua")
    void consultarReservasConFiltros() {
        // Arrange: tres reservas creadas en momentos distintos
        Reserva antigua = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        reloj = relojEn(FichaStaylyUq.HOY.plusHours(2));
        Reserva media = crearReserva().ejecutar(new CrearReservaUseCase.Comando(suq201.identificacion(), VEINTE_NOV,
                VEINTIDOS_NOV, ocupantes(), CanalOrigen.DIRECTO, null, null, 0, "recepcion")).reserva();
        reloj = relojEn(FichaStaylyUq.HOY.plusHours(4));
        Reserva reciente = crearReserva().ejecutar(new CrearReservaUseCase.Comando(suq301.identificacion(),
                LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 27), ocupantes(), CanalOrigen.PORTAL, null, null, 0,
                "huesped")).reserva();
        reciente.cancelar("huesped", FichaStaylyUq.HOY.plusHours(5));
        ConsultarReservasUseCase consultar = new ConsultarReservasUseCase(reservas);

        // Act + Assert
        assertThat(consultar.ejecutar(FiltroReservas.ninguno(), PeticionPagina.primera()).contenido())
                .containsExactly(reciente, media, antigua);
        assertThat(consultar.ejecutar(new FiltroReservas(EstadoReserva.CANCELADA, null, null, null, null, null),
                PeticionPagina.primera()).contenido()).containsExactly(reciente);
        assertThat(consultar.ejecutar(new FiltroReservas(null, CanalOrigen.DIRECTO, null, "laura", null, null),
                PeticionPagina.primera()).contenido()).containsExactly(media);
    }

    @Test
    @DisplayName("7.5 · El calendario marca cada noche como reservada, bloqueada o libre")
    void consultarCalendario() {
        // Arrange
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        bloqueos.guardar(Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), suq301.identificacion(),
                new RangoFechas(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 25)), "Pintura", "admin",
                FichaStaylyUq.HOY));

        // Act
        List<NocheOcupacion> noches = new ConsultarCalendarioOcupacionUseCase(apartamentos, reservas, bloqueos)
                .ejecutar(new RangoFechas(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 26)),
                        suq301.identificacion())
                .apartamentos().get(0).noches();

        // Assert
        assertThat(noches).extracting(NocheOcupacion::estado).containsExactly(EstadoNoche.LIBRE,
                EstadoNoche.RESERVADA, EstadoNoche.RESERVADA, EstadoNoche.LIBRE, EstadoNoche.BLOQUEADA,
                EstadoNoche.BLOQUEADA, EstadoNoche.LIBRE);
        assertThat(noches.get(1).reserva()).isEqualTo(reserva.codigo());
    }

    // Ensamblaje de los casos de uso con el reloj actual

    private CrearReservaUseCase crearReserva() {
        return new CrearReservaUseCase(apartamentos, reservas, folios, politicas, verificadorTarifas,
                new VerificadorEstanciaMinimaService(temporadas), admisionMascotas, disponibilidad(), cotizador,
                configuracion, reloj);
    }

    private ModificarReservaUseCase modificar() {
        return new ModificarReservaUseCase(reservas, apartamentos, folios, verificadorTarifas,
                new VerificadorEstanciaMinimaService(temporadas), admisionMascotas, disponibilidad(), cotizador,
                configuracion, reloj);
    }

    private CotizarEstanciaUseCase cotizar() {
        return new CotizarEstanciaUseCase(apartamentos, verificadorTarifas, cotizador, admisionMascotas, configuracion);
    }

    private BuscarApartamentosDisponiblesUseCase buscar() {
        return new BuscarApartamentosDisponiblesUseCase(apartamentos, disponibilidad(), verificadorTarifas, cotizador,
                reloj);
    }

    private DeclararNoShowUseCase noShow() {
        return new DeclararNoShowUseCase(reservas, folios, new CalculadorRetencionService(politicas), configuracion,
                reloj);
    }

    private VencerReservasPendientesUseCase vencer() {
        return new VencerReservasPendientesUseCase(reservas, folios, configuracion, reloj);
    }

    private DisponibilidadApartamentoService disponibilidad() {
        return new DisponibilidadApartamentoService(reservas, bloqueos, configuracion);
    }

    private Reserva reservaConfirmada() {
        Reserva reserva = crearReserva().ejecutar(comandoSuq301(LocalTime.of(16, 0), 0)).reserva();
        folios.buscarPorCodigoReserva(reserva.codigo()).orElseThrow().registrarPago(
                new Pago(Dinero.pesos(117_000), MedioPago.TRANSFERENCIA, FichaStaylyUq.HOY, "TRF-1"));
        new ConfirmarReservaUseCase(reservas, folios, new ConfirmacionReservaService(configuracion), reloj)
                .ejecutar(reserva.codigo(), "recepcion");
        return reserva;
    }

    private List<String> nombresDisponibles(Criterios criterios) {
        return buscar().ejecutar(criterios, PeticionPagina.primera()).contenido().stream()
                .map(disponible -> disponible.apartamento().identificacion().valor())
                .toList();
    }

    /** SUQ-301 del 20 al 22/11/2026 (temporada base) con dos adultos y una niña de 13 años. */
    private CrearReservaUseCase.Comando comandoSuq301(LocalTime horaLlegada, int mascotas) {
        return new CrearReservaUseCase.Comando(suq301.identificacion(), VEINTE_NOV, VEINTIDOS_NOV, ocupantes(),
                CanalOrigen.PORTAL, null, horaLlegada, mascotas, "laura@correo.co");
    }

    private ModificarReservaUseCase.Comando comandoModificar(CodigoReserva codigo, LocalDate salida, int mascotas) {
        return new ModificarReservaUseCase.Comando(codigo, suq301.identificacion(), VEINTE_NOV, salida, ocupantes(),
                mascotas, "recepcion");
    }

    private static List<CrearReservaUseCase.DatosOcupante> ocupantes() {
        return List.of(
                new CrearReservaUseCase.DatosOcupante("Laura Gómez", LocalDate.of(1990, 5, 10), "1094000001", true,
                        "laura@correo.co", "3001234567"),
                new CrearReservaUseCase.DatosOcupante("Andrés Ríos", LocalDate.of(1988, 2, 1), null, false, null, null),
                new CrearReservaUseCase.DatosOcupante("Sara Ríos", LocalDate.of(2013, 4, 2), null, false, null, null));
    }

    private static List<LocalDate> grupoDeTres() {
        return List.of(LocalDate.of(1990, 5, 10), LocalDate.of(1988, 2, 1), LocalDate.of(2013, 4, 2));
    }

    private static Clock relojEn(LocalDateTime momento) {
        return Clock.fixed(momento.atZone(BOGOTA).toInstant(), BOGOTA);
    }
}

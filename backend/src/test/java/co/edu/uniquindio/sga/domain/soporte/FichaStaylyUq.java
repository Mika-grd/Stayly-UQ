package co.edu.uniquindio.sga.domain.soporte;

import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.CargoNoche;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Datos de prueba coherentes con la Ficha de Stayly-UQ (Anexo A y Anexo B). Todas las fechas
 * son fijas: "hoy" es el 1 de octubre de 2026.
 */
public final class FichaStaylyUq {

    public static final LocalDateTime HOY = LocalDateTime.of(2026, 10, 1, 10, 0);
    public static final UmbralEdadFacturable UMBRAL = new UmbralEdadFacturable(12);
    public static final LocalDateTime VIGENCIA_TARIFAS = LocalDateTime.of(2026, 1, 1, 0, 0);

    /** Temporadas de la Ficha. Los rangos son [inicio, fin): la fecha fin no se incluye. */
    public static final IdTemporada ID_BASE = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000b"));
    public static final IdTemporada ID_ALTA = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
    public static final IdTemporada ID_MEDIA = new IdTemporada(UUID.fromString("00000000-0000-0000-0000-00000000000c"));

    private static int consecutivo;

    private FichaStaylyUq() {
    }

    public static Temporada temporadaBase() {
        return Temporada.crearBase(ID_BASE, "Base");
    }

    public static Temporada temporadaAlta() {
        return Temporada.crear(ID_ALTA, "Alta", List.of(
                new RangoFechas(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16)),
                new RangoFechas(LocalDate.of(2027, 3, 21), LocalDate.of(2027, 3, 29))),
                new EstanciaMinima(2));
    }

    public static Temporada temporadaMedia() {
        return Temporada.crear(ID_MEDIA, "Media", List.of(
                new RangoFechas(LocalDate.of(2027, 6, 15), LocalDate.of(2027, 7, 16))),
                new EstanciaMinima(1));
    }

    /** SUQ-201: 2 dormitorios, capacidad 4, no admite mascotas. Activo y PREPARADO. */
    public static Apartamento suq201() {
        return activoYPreparado(Apartamento.crear(new IdentificacionApartamento("SUQ-201"), "Apartamento 201",
                "Dos dormitorios en el segundo piso", 2, new Capacidad(4), false, null,
                Set.of(new Caracteristica("Cocina equipada")), imagenes(1)));
    }

    /** SUQ-102: 1 dormitorio, capacidad 2, admite mascotas, sin escaleras. */
    public static Apartamento suq102() {
        return activoYPreparado(Apartamento.crear(new IdentificacionApartamento("SUQ-102"), "Apartamento 102",
                "Un dormitorio en el primer piso, apto para movilidad reducida", 1, new Capacidad(2), true, null,
                Set.of(new Caracteristica("Admite mascotas")), imagenes(1)));
    }

    /** SUQ-301: 3 dormitorios, capacidad 6, admite mascotas, tercer piso sin ascensor. */
    public static Apartamento suq301() {
        return activoYPreparado(Apartamento.crear(new IdentificacionApartamento("SUQ-301"), "Apartamento 301",
                "Tres dormitorios para grupos grandes", 3, new Capacidad(6), true, "Tercer piso sin ascensor",
                Set.of(new Caracteristica("Admite mascotas")), imagenes(1)));
    }

    public static Apartamento activoYPreparado(Apartamento apartamento) {
        apartamento.activar();
        apartamento.iniciarPreparacion();
        apartamento.marcarPreparado();
        return apartamento;
    }

    public static List<ImagenApartamento> imagenes(int cantidad) {
        List<ImagenApartamento> imagenes = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            imagenes.add(new ImagenApartamento("https://stayly-uq.co/img/" + i + ".jpg", i == 1));
        }
        return imagenes;
    }

    /** Tarifas por ocupante facturable por noche (base / alta / media). */
    public static Tarifario tarifario(String apartamento, long base, long alta, long media) {
        Tarifario tarifario = Tarifario.nuevo(new IdentificacionApartamento(apartamento));
        tarifario.definirTarifa(ID_BASE, Dinero.pesos(base), VIGENCIA_TARIFAS);
        tarifario.definirTarifa(ID_ALTA, Dinero.pesos(alta), VIGENCIA_TARIFAS);
        tarifario.definirTarifa(ID_MEDIA, Dinero.pesos(media), VIGENCIA_TARIFAS);
        return tarifario;
    }

    public static Tarifario tarifarioSuq201() {
        return tarifario("SUQ-201", 75_000, 101_000, 86_000);
    }

    /** Política v1: ≥15 días 0 %, 7–14 días 30 %, menos de 7 días 60 %; no-show 30 %. */
    public static PoliticaCancelacion politicaV1() {
        return PoliticaCancelacion.crearVersion(new VersionPolitica(1), List.of(
                new TramoPolitica(15, 0), new TramoPolitica(7, 30), new TramoPolitica(0, 60)),
                30, VIGENCIA_TARIFAS);
    }

    public static Ocupante titularAdulto() {
        return Ocupante.titular(new IdOcupante(UUID.randomUUID()), "Laura Gómez", LocalDate.of(1990, 5, 10),
                new DocumentoIdentidad("1094000001"), new DatosContactoTitular("laura@correo.co", "3001234567"));
    }

    public static Ocupante adulto() {
        return Ocupante.acompanante(new IdOcupante(UUID.randomUUID()), "Andrés Ríos", LocalDate.of(1988, 2, 1),
                new DocumentoIdentidad("1094000002"));
    }

    /** Niño de 6 años en diciembre de 2026. */
    public static Ocupante nino() {
        return Ocupante.acompanante(new IdOcupante(UUID.randomUUID()), "Tomás Ríos", LocalDate.of(2020, 3, 15), null);
    }

    /** Valor de una tarifa fija por noche, para pruebas que no dependen del calendario. */
    public static ValorCongelado valorFijo(Estancia estancia, long tarifa, int facturables) {
        return new ValorCongelado(estancia.fechasDeNoches().stream()
                .map(noche -> new CargoNoche(noche, "Base", Dinero.pesos(tarifa), facturables))
                .toList());
    }

    /** Ejemplo del Anexo B: SUQ-201 del 13 al 17/12/2026 con 2 facturables = $704.000. */
    public static ValorCongelado valorAnexoB() {
        Dinero base = Dinero.pesos(75_000);
        Dinero alta = Dinero.pesos(101_000);
        return new ValorCongelado(List.of(
                new CargoNoche(LocalDate.of(2026, 12, 13), "Base", base, 2),
                new CargoNoche(LocalDate.of(2026, 12, 14), "Base", base, 2),
                new CargoNoche(LocalDate.of(2026, 12, 15), "Alta", alta, 2),
                new CargoNoche(LocalDate.of(2026, 12, 16), "Alta", alta, 2)));
    }

    public static CodigoReserva siguienteCodigo() {
        return new CodigoReserva("RES-2026-%05d".formatted(++consecutivo));
    }

    /** Reserva PENDIENTE creada "hoy" (01/10/2026) por el portal, con un titular y un adulto. */
    public static Reserva reservaPendiente(Apartamento apartamento, Estancia estancia) {
        return reservaPendiente(apartamento, estancia, valorFijo(estancia, 75_000, 2));
    }

    public static Reserva reservaPendiente(Apartamento apartamento, Estancia estancia, ValorCongelado valor) {
        return Reserva.crear(siguienteCodigo(), apartamento.identificacion(), estancia,
                List.of(titularAdulto(), adulto()), CanalOrigen.PORTAL, null, null, MascotasAutorizadas.ninguna(),
                valor, new VersionPolitica(1), UMBRAL, "huesped@correo.co", HOY);
    }

    public static Reserva reservaExterna(Apartamento apartamento, Estancia estancia, IdentificadorExterno externo) {
        return Reserva.crear(siguienteCodigo(), apartamento.identificacion(), estancia,
                List.of(titularAdulto(), adulto()), CanalOrigen.EXTERNO, externo, null, MascotasAutorizadas.ninguna(),
                valorFijo(estancia, 75_000, 2), new VersionPolitica(1), UMBRAL, "canal-externo", HOY);
    }

    /** Reserva CONFIRMADA: con hora estimada de llegada a las 16:00. */
    public static Reserva reservaConfirmada(Apartamento apartamento, Estancia estancia) {
        Reserva reserva = reservaPendiente(apartamento, estancia);
        reserva.indicarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(16, 0)), "recepcion", HOY);
        reserva.confirmar("recepcion", HOY.plusHours(1));
        return reserva;
    }


    public static Estancia diciembre(int entrada, int salida) {
        return new Estancia(LocalDate.of(2026, 12, entrada), LocalDate.of(2026, 12, salida));
    }
}

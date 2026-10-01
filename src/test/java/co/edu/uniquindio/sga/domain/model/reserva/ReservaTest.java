package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifarioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.UMBRAL;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.adulto;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.titularAdulto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    private final Apartamento suq201 = FichaStaylyUq.suq201();

    @Test
    @DisplayName("PU-05 · No crea una reserva con entrada anterior a hoy (RN-04)")
    void noCrearReservaHaciaElPasado() {
        // Arrange
        Estancia estanciaPasada = new Estancia(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 27));

        // Act + Assert
        assertThatThrownBy(() -> Reserva.crear(new CodigoReserva("RES-2026-00042"), suq201.identificacion(),
                estanciaPasada, List.of(titularAdulto(), adulto()), CanalOrigen.PORTAL, null, null,
                MascotasAutorizadas.ninguna(), FichaStaylyUq.valorFijo(estanciaPasada, 75_000, 2),
                new VersionPolitica(1), UMBRAL, "huesped@correo.co", HOY))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("La fecha de entrada no puede ser anterior a la fecha actual.")
                .extracting("codigo").isEqualTo("FECHA_ENTRADA_PASADA");
    }

    @Test
    @DisplayName("PU-06 · No confirma una reserva sin hora estimada de llegada (RN-09)")
    void noConfirmarSinHoraEstimada() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(13, 15));

        // Act + Assert
        assertThatThrownBy(() -> reserva.confirmar("recepcion", HOY.plusHours(2)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("HORA_LLEGADA_REQUERIDA");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    // Pruebas adicionales de la Matriz de Trazabilidad

    @Test
    @DisplayName("RN-10 · No registra la llegada antes de la fecha de entrada")
    void noRegistrarAntesDeFechaEntrada() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, FichaStaylyUq.diciembre(13, 15));
        LocalDateTime diaAnterior = LocalDateTime.of(2026, 12, 12, 18, 0);

        // Act + Assert
        assertThat(reserva.puedeRegistrarLlegada(diaAnterior.toLocalDate())).isFalse();
        assertThatThrownBy(() -> reserva.registrarLlegada("recepcion", diaAnterior))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("REGISTRO_ANTES_DE_ENTRADA");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("RN-10 · No registra la llegada de una reserva PENDIENTE")
    void noRegistrarReservaPendiente() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(13, 15));
        LocalDateTime diaDeEntrada = LocalDateTime.of(2026, 12, 13, 15, 0);

        // Act + Assert
        assertThat(reserva.puedeRegistrarLlegada(diaDeEntrada.toLocalDate())).isFalse();
        assertThatThrownBy(() -> reserva.registrarLlegada("recepcion", diaDeEntrada))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TRANSICION_INVALIDA");
    }

    @Test
    @DisplayName("RN-14 · Modificar a más noches produce un ajuste positivo")
    void modificarProduceAjustePositivo() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(13, 15));
        Estancia nuevaEstancia = FichaStaylyUq.diciembre(13, 16);
        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva.codigo(), reserva.valor().total(), HOY);

        // Act
        Dinero diferencia = reserva.modificar(suq201.identificacion(), nuevaEstancia, reserva.ocupantes(),
                MascotasAutorizadas.ninguna(), FichaStaylyUq.valorFijo(nuevaEstancia, 75_000, 2), UMBRAL,
                "recepcion", HOY.plusDays(1));
        folio.registrarAjuste(diferencia, HOY.plusDays(1));

        // Assert
        assertThat(diferencia).isEqualTo(Dinero.pesos(150_000));
        assertThat(reserva.estancia()).isEqualTo(nuevaEstancia);
        assertThat(reserva.versionPolitica()).isEqualTo(new VersionPolitica(1));
        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(450_000));
    }

    @Test
    @DisplayName("RN-14 · Modificar a menos ocupantes facturables produce un ajuste negativo")
    void modificarProduceAjusteNegativo() {
        // Arrange
        Estancia estancia = FichaStaylyUq.diciembre(13, 15);
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, estancia);
        List<Ocupante> soloElTitular = List.of(reserva.titular());

        // Act
        Dinero diferencia = reserva.modificar(suq201.identificacion(), estancia, soloElTitular,
                MascotasAutorizadas.ninguna(), FichaStaylyUq.valorFijo(estancia, 75_000, 1), UMBRAL,
                "recepcion", HOY.plusDays(1));

        // Assert
        assertThat(diferencia).isEqualTo(Dinero.pesos(-150_000));
        assertThat(reserva.totalOcupantes()).isEqualTo(1);
    }

    @Test
    @DisplayName("RN-14 · No modifica una reserva EN_CURSO")
    void noModificarReservaEnCurso() {
        // Arrange
        Estancia estancia = FichaStaylyUq.diciembre(13, 15);
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, estancia);
        reserva.registrarLlegada("recepcion", LocalDateTime.of(2026, 12, 13, 15, 0));

        // Act + Assert
        assertThatThrownBy(() -> reserva.modificar(suq201.identificacion(), FichaStaylyUq.diciembre(13, 16),
                reserva.ocupantes(), MascotasAutorizadas.ninguna(), FichaStaylyUq.valorFijo(estancia, 75_000, 2),
                UMBRAL, "recepcion", LocalDateTime.of(2026, 12, 13, 18, 0)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("RESERVA_NO_MODIFICABLE");
    }

    @Test
    @DisplayName("RN-21 · Una reserva PENDIENTE fuera del plazo de confirmación se cancela sola")
    void vencerReservaFueraDePlazo() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(13, 15));
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);

        // Act
        reserva.vencer(plazo, HOY.plusHours(24));

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(reserva.estaActiva()).isFalse();
    }

    @Test
    @DisplayName("RN-21 · No vence una reserva PENDIENTE dentro del plazo de confirmación")
    void noVencerDentroDelPlazo() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, FichaStaylyUq.diciembre(13, 15));
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);

        // Act + Assert
        assertThatThrownBy(() -> reserva.vencer(plazo, HOY.plusHours(23)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TRANSICION_INVALIDA");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("RN-22 · Un cambio de tarifa no altera una reserva ya creada")
    void cambioDeTarifaNoAlteraReservaCreada() {
        // Arrange
        TemporadaRepositoryEnMemoria temporadas = new TemporadaRepositoryEnMemoria();
        temporadas.guardar(FichaStaylyUq.temporadaBase());
        temporadas.guardar(FichaStaylyUq.temporadaAlta());
        TarifarioRepositoryEnMemoria tarifarios = new TarifarioRepositoryEnMemoria();
        Tarifario tarifario = FichaStaylyUq.tarifarioSuq201();
        tarifarios.guardar(tarifario);
        CotizadorEstanciaService cotizador =
                new CotizadorEstanciaService(temporadas, tarifarios, ConfiguracionAlojamientoFake.ficha());
        Estancia estancia = FichaStaylyUq.diciembre(14, 16);
        Reserva reserva = FichaStaylyUq.reservaPendiente(suq201, estancia,
                cotizador.cotizar(suq201.identificacion(), estancia, new ComposicionGrupo(List.of(
                        LocalDate.of(1990, 5, 10), LocalDate.of(1988, 2, 1)))));

        // Act
        tarifario.definirTarifa(FichaStaylyUq.ID_ALTA, Dinero.pesos(120_000), HOY.plusDays(1));

        // Assert
        assertThat(reserva.valor().total()).isEqualTo(Dinero.pesos(352_000));
        assertThat(tarifario.historico()).hasSize(4);
        assertThat(cotizador.cotizar(suq201.identificacion(), estancia, reserva.composicion()).total())
                .isEqualTo(Dinero.pesos(390_000));
    }
}

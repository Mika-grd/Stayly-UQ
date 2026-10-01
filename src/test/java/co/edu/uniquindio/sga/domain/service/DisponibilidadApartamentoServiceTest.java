package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisponibilidadApartamentoServiceTest {

    private final ReservaRepositoryEnMemoria reservas =
            new ReservaRepositoryEnMemoria(Clock.fixed(HOY.atZone(ZoneId.of("America/Bogota")).toInstant(),
                    ZoneId.of("America/Bogota")));
    private final BloqueoRepositoryEnMemoria bloqueos = new BloqueoRepositoryEnMemoria();
    private final ConfiguracionAlojamientoFake configuracion = ConfiguracionAlojamientoFake.ficha();
    private final DisponibilidadApartamentoService servicio =
            new DisponibilidadApartamentoService(reservas, bloqueos, configuracion);
    private final Apartamento suq201 = FichaStaylyUq.suq201();

    @Test
    @DisplayName("PU-09 · No permite solapar una reserva activa (RN-01)")
    void noPermitirSolapamientoReservaActiva() {
        // Arrange
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12)));

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarDisponible(suq201, diciembre(11, 14), 2, null))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("El apartamento ya tiene una reserva activa que solapa esas noches.")
                .extracting("codigo").isEqualTo("NOCHES_NO_DISPONIBLES");
    }

    @Test
    @DisplayName("PU-10 · No admite más ocupantes que la capacidad (RN-02)")
    void noAdmitirMasOcupantesQueCapacidad() {
        // Arrange
        int grupoDeCinco = 5;

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarDisponible(suq201, diciembre(11, 14), grupoDeCinco, null))
                .isInstanceOf(ReglaDominioException.class)
                .hasMessage("El número de ocupantes excede la capacidad del apartamento.")
                .extracting("codigo").isEqualTo("CAPACIDAD_EXCEDIDA");
    }

    // Pruebas adicionales de la Matriz de Trazabilidad

    @Test
    @DisplayName("RN-07 · No permite reservar sobre un bloqueo vigente")
    void noPermitirReservaSobreBloqueo() {
        // Arrange
        bloqueos.guardar(Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), suq201.identificacion(),
                new RangoFechas(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23)), "Daño en la tubería",
                "admin@stayly-uq.co", HOY));

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarDisponible(suq201, diciembre(22, 24), 2, null))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_BLOQUEADO");
    }

    @Test
    @DisplayName("RN-07 · Permite reservar fuera de las noches del bloqueo")
    void permitirReservaFueraDelBloqueo() {
        // Arrange
        bloqueos.guardar(Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), suq201.identificacion(),
                new RangoFechas(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23)), "Daño en la tubería",
                "admin@stayly-uq.co", HOY));

        // Act
        boolean disponible = servicio.estaDisponible(suq201, diciembre(23, 26), 2);

        // Assert
        assertThat(disponible).isTrue();
    }

    @Test
    @DisplayName("RN-12 · Una reserva cancelada libera sus noches de inmediato")
    void reservaCanceladaLiberaNoches() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12));
        reservas.guardar(reserva);
        reserva.cancelar("recepcion", HOY.plusDays(2));

        // Act
        boolean disponible = servicio.estaDisponible(suq201, diciembre(10, 12), 2);

        // Assert
        assertThat(disponible).isTrue();
    }

    @Test
    @DisplayName("RN-12 · Una reserva en no-show libera sus noches de inmediato")
    void reservaNoShowLiberaNoches() {
        // Arrange
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12));
        reservas.guardar(reserva);
        reserva.declararNoShow(configuracion.horaLimiteNoShow(), "recepcion", LocalDateTime.of(2026, 12, 10, 23, 5));

        // Act
        boolean disponible = servicio.estaDisponible(suq201, diciembre(11, 12), 2);

        // Assert
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.NO_SHOW);
        assertThat(disponible).isTrue();
    }

    @Test
    @DisplayName("RN-20 · No permite entrar el día de una salida si no alcanza el tiempo de preparación")
    void noPermitirEntradaMismoDiaSiNoAlcanzaPreparacion() {
        // Arrange
        configuracion.conTiempoPreparacion(4);
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12)));

        // Act + Assert
        assertThatThrownBy(() -> servicio.verificarDisponible(suq201, diciembre(12, 14), 2, null))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("TIEMPO_PREPARACION_INSUFICIENTE");
    }

    @Test
    @DisplayName("RN-20 · Permite entrar el día de una salida si el tiempo de preparación cabe (3 h entre 12:00 y 15:00)")
    void permitirSiAlcanza() {
        // Arrange
        reservas.guardar(FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12)));

        // Act
        boolean disponible = servicio.estaDisponible(suq201, diciembre(12, 14), 2);

        // Assert
        assertThat(disponible).isTrue();
    }
}

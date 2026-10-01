package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.canal.EstadoConflicto;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.model.canal.TipoConciliacion;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ConflictoCanalRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;

class ConciliacionCanalExternoServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 2, 8, 0);
    private final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria(Clock.systemDefaultZone());
    private final ConciliacionCanalExternoService servicio = new ConciliacionCanalExternoService(reservas,
            new DisponibilidadApartamentoService(reservas, new BloqueoRepositoryEnMemoria(),
                    ConfiguracionAlojamientoFake.ficha()));
    private final Apartamento suq201 = FichaStaylyUq.suq201();
    private final IdCanal simulador = new IdCanal(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    @Test
    @DisplayName("RN-18 · Rechaza la reserva externa en conflicto y la registra como ConflictoCanal")
    void rechazarYRegistrarConflicto() {
        // Arrange
        Reserva vigente = FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12));
        reservas.guardar(vigente);
        ConflictoCanalRepositoryEnMemoria conflictos = new ConflictoCanalRepositoryEnMemoria();

        // Act
        ResultadoConciliacion resultado = servicio.conciliar(new IdentificadorExterno(simulador, "EXT-9001"), suq201,
                diciembre(11, 13), 2, AHORA);
        conflictos.guardar(resultado.conflicto());

        // Assert
        assertThat(resultado.tipo()).isEqualTo(TipoConciliacion.CONFLICTO);
        assertThat(resultado.conflicto().estado()).isEqualTo(EstadoConflicto.PENDIENTE);
        assertThat(resultado.conflicto().reservaVigente()).isEqualTo(vigente.codigo());
        assertThat(conflictos.listarPendientes()).containsExactly(resultado.conflicto());
    }

    @Test
    @DisplayName("RN-18 · Nunca sobrescribe la reserva vigente")
    void noSobrescribirReservaVigente() {
        // Arrange
        Reserva vigente = FichaStaylyUq.reservaConfirmada(suq201, diciembre(10, 12));
        reservas.guardar(vigente);

        // Act
        servicio.conciliar(new IdentificadorExterno(simulador, "EXT-9002"), suq201, diciembre(10, 12), 2, AHORA);

        // Assert
        Reserva guardada = reservas.buscarPorCodigo(vigente.codigo()).orElseThrow();
        assertThat(guardada.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(guardada.estancia()).isEqualTo(diciembre(10, 12));
        assertThat(reservas.buscarActivasQueSolapan(suq201.identificacion(), diciembre(10, 12))).hasSize(1);
    }

    @Test
    @DisplayName("RN-19 · Un mensaje repetido no crea dos reservas")
    void mensajeRepetidoNoDuplica() {
        // Arrange
        IdentificadorExterno externo = new IdentificadorExterno(simulador, "EXT-9003");
        Reserva yaCreada = FichaStaylyUq.reservaExterna(suq201, diciembre(20, 22), externo);
        reservas.guardar(yaCreada);

        // Act
        ResultadoConciliacion resultado = servicio.conciliar(new IdentificadorExterno(simulador, "EXT-9003"), suq201,
                diciembre(20, 22), 2, AHORA);

        // Assert
        assertThat(resultado.tipo()).isEqualTo(TipoConciliacion.YA_EXISTE);
        assertThat(resultado.reservaExistente()).isEqualTo(yaCreada.codigo());
    }
}

package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.NotificadorTitular.Motivo;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NotificadorTitularCorreoTest {

    private final List<String[]> enviados = new ArrayList<>();
    private final NotificadorCorreo buzon =
            (destinatario, asunto, cuerpo) -> enviados.add(new String[]{destinatario, asunto, cuerpo});
    private final Reserva reserva =
            FichaStaylyUq.reservaPendiente(FichaStaylyUq.suq201(), FichaStaylyUq.diciembre(10, 13));

    @AfterEach
    void limpiar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private NotificadorTitularCorreo notificador(NotificadorCorreo correo) {
        return new NotificadorTitularCorreo(correo, ConfiguracionAlojamientoFake.ficha(), Runnable::run);
    }

    @Test
    @DisplayName("7.5 · Al crear la reserva se avisa al correo del titular con el código y la estancia")
    void avisaAlTitular() {
        // Act
        notificador(buzon).notificar(reserva, Motivo.CREADA);

        // Assert
        assertThat(enviados).hasSize(1);
        assertThat(enviados.get(0)[0]).isEqualTo("laura@correo.co");
        assertThat(enviados.get(0)[1]).contains("recibimos su reserva", reserva.codigo().valor());
        assertThat(enviados.get(0)[2]).contains("Laura Gómez", "SUQ-201", "3 noches");
    }

    @Test
    @DisplayName("I-05 · El aviso sale después del commit: si la operación se revierte, no se avisa")
    void soloDespuesDelCommit() {
        // Arrange
        TransactionSynchronizationManager.initSynchronization();

        // Act
        notificador(buzon).notificar(reserva, Motivo.CONFIRMADA);

        // Assert: nada todavía; al confirmar la transacción, el aviso
        assertThat(enviados).isEmpty();
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(enviados).hasSize(1);
        assertThat(enviados.get(0)[1]).contains("confirmada");
    }

    @Test
    @DisplayName("I-04 · Si el proveedor de correo falla, la operación de negocio no se entera (caso de falla)")
    void fallaDelProveedorNoSePropaga() {
        // Arrange
        NotificadorCorreo caido = (destinatario, asunto, cuerpo) -> {
            throw new IllegalStateException("SMTP no responde");
        };
        NotificadorTitularCorreo colaLlena = new NotificadorTitularCorreo(buzon, ConfiguracionAlojamientoFake.ficha(),
                tarea -> {
                    throw new RejectedExecutionException("cola llena");
                });

        // Act y Assert
        assertThatCode(() -> notificador(caido).notificar(reserva, Motivo.CANCELADA)).doesNotThrowAnyException();
        assertThatCode(() -> colaLlena.notificar(reserva, Motivo.CANCELADA)).doesNotThrowAnyException();
        assertThat(enviados).isEmpty();
    }
}

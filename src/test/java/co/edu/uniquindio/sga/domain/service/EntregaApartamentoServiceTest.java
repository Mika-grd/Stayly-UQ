package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.diciembre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntregaApartamentoServiceTest {

    private final EntregaApartamentoService servicio = new EntregaApartamentoService();

    @Test
    @DisplayName("PU-12 · No entrega un apartamento que está en preparación (RN-11)")
    void noEntregarApartamentoEnPreparacion() {
        // Arrange
        Apartamento suq201 = Apartamento.crear(new IdentificacionApartamento("SUQ-201"), "Apartamento 201",
                "Dos dormitorios en el segundo piso", 2, new Capacidad(4), false, null, Set.of(),
                FichaStaylyUq.imagenes(1));
        suq201.activar();
        suq201.iniciarPreparacion();
        Reserva reserva = FichaStaylyUq.reservaConfirmada(suq201, diciembre(13, 15));
        LocalDateTime diaDeEntrada = LocalDateTime.of(2026, 12, 13, 15, 0);

        // Act + Assert
        assertThatThrownBy(() -> servicio.registrarLlegada(reserva, suq201, "recepcion", diaDeEntrada))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("APARTAMENTO_NO_PREPARADO");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(suq201.estadoOperativo()).isEqualTo(EstadoOperativo.EN_PREPARACION);
    }
}

package co.edu.uniquindio.sga.domain.model.canal;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConflictoCanalTest {

    private static final IdentificadorExterno EXTERNO =
            new IdentificadorExterno(new IdCanal(UUID.randomUUID()), "BK-7781");
    private static final Estancia ESTANCIA = new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 15));
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 1, 10, 0);

    // Reconstitución desde la persistencia

    @Test
    @DisplayName("Persistencia · Un conflicto reconstituido como RESUELTO no se puede resolver otra vez (CC-1)")
    void reconstituirConflictoResuelto() {
        // Act
        ConflictoCanal conflicto = ConflictoCanal.reconstituir(new IdConflictoCanal(UUID.randomUUID()), EXTERNO,
                new IdentificacionApartamento("SUQ-201"), ESTANCIA, new CodigoReserva("RES-2026-00003"), REGISTRO,
                EstadoConflicto.RESUELTO, "admin@stayly-uq.co", "Se reubicó al huésped del canal en SUQ-202",
                REGISTRO.plusHours(3));

        // Assert
        assertThat(conflicto.estado()).isEqualTo(EstadoConflicto.RESUELTO);
        assertThat(conflicto.autorResolucion()).contains("admin@stayly-uq.co");
        assertThatThrownBy(() -> conflicto.resolver("admin@stayly-uq.co", "Otra vez", REGISTRO.plusDays(1)))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("CONFLICTO_YA_RESUELTO");
    }

    @Test
    @DisplayName("Persistencia · No reconstituye un conflicto RESUELTO sin autor ni resolución (CC-1)")
    void noReconstituirResueltoSinAutor() {
        assertThatThrownBy(() -> ConflictoCanal.reconstituir(new IdConflictoCanal(UUID.randomUUID()), EXTERNO,
                new IdentificacionApartamento("SUQ-201"), ESTANCIA, new CodigoReserva("RES-2026-00003"), REGISTRO,
                EstadoConflicto.RESUELTO, null, null, null))
                .isInstanceOf(ReglaDominioException.class)
                .extracting("codigo").isEqualTo("PARAMETRO_INVALIDO");
    }
}

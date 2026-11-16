package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.AsistenteIA;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ResumirDiaUseCaseTest {

    private static final LocalDate DIEZ_DIC = LocalDate.of(2026, 12, 10);

    private final AtomicReference<String> datosEnviados = new AtomicReference<>();
    private ReservaRepositoryEnMemoria reservas;
    private ApartamentoRepositoryEnMemoria apartamentos;

    @BeforeEach
    void preparar() {
        ZoneId bogota = ZoneId.of("America/Bogota");
        Clock reloj = Clock.fixed(FichaStaylyUq.HOY.atZone(bogota).toInstant(), bogota);
        reservas = new ReservaRepositoryEnMemoria(reloj);
        apartamentos = new ApartamentoRepositoryEnMemoria();
        Reserva llegada = FichaStaylyUq.reservaConfirmada(FichaStaylyUq.suq201(), FichaStaylyUq.diciembre(10, 13));
        reservas.guardar(llegada);
    }

    private ResumirDiaUseCase conAsistente(AsistenteIA asistente) {
        return new ResumirDiaUseCase(new ConsultarMovimientosDelDiaUseCase(reservas, Clock.systemDefaultZone()),
                new ConsultarApartamentosPorPrepararUseCase(apartamentos), asistente);
    }

    private AsistenteIA responde(Optional<String> texto) {
        return new AsistenteIA() {
            @Override
            public Optional<String> redactar(String instruccion, String datos) {
                datosEnviados.set(datos);
                return texto;
            }

            @Override
            public String proveedor() {
                return "modelo-de-prueba";
            }
        };
    }

    @Test
    @DisplayName("10.2 · Con el modelo disponible, el resumen trae su texto y el proveedor")
    void resumenConIA() {
        // Act
        var resumen = conAsistente(responde(Optional.of("  Llega un grupo al 201 a las 16:00.  "))).ejecutar(DIEZ_DIC);

        // Assert
        assertThat(resumen.disponible()).isTrue();
        assertThat(resumen.texto()).contains("Llega un grupo al 201 a las 16:00.");
        assertThat(resumen.proveedor()).isEqualTo("modelo-de-prueba");
    }

    @Test
    @DisplayName("10.2 · Al modelo solo van códigos, apartamentos y horas: ningún dato personal del huésped")
    void sinDatosPersonales() {
        // Act
        conAsistente(responde(Optional.of("ok"))).ejecutar(DIEZ_DIC);

        // Assert
        assertThat(datosEnviados.get()).contains("SUQ-201", "16:00", "Llegadas (1)")
                .doesNotContain("Laura", "1094000001", "laura@correo.co", "3001234567");
    }

    @Test
    @DisplayName("10.2 · Si el modelo no responde o falla, el resumen queda no disponible (caso de falla)")
    void sinIA() {
        // Arrange
        AsistenteIA roto = new AsistenteIA() {
            @Override
            public Optional<String> redactar(String instruccion, String datos) {
                throw new IllegalStateException("modelo caído");
            }

            @Override
            public String proveedor() {
                return "roto";
            }
        };

        // Act
        var vacio = conAsistente(responde(Optional.empty())).ejecutar(DIEZ_DIC);
        var fallido = conAsistente(roto).ejecutar(DIEZ_DIC);

        // Assert
        assertThat(vacio.disponible()).isFalse();
        assertThat(fallido.disponible()).isFalse();
        assertThat(fallido.fecha()).isEqualTo(DIEZ_DIC);
    }
}

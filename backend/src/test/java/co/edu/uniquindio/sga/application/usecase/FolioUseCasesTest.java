package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.EstadoFolio;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.soporte.ConfiguracionAlojamientoFake;
import co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static co.edu.uniquindio.sga.domain.soporte.FichaStaylyUq.HOY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 7.7 con repositorios en memoria: pagos, cargos manuales y cierre del folio (CU-07, CU-18). */
class FolioUseCasesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String RECEPCION = "recepcion@stayly.local";
    private static final String ADMIN = "admin@stayly.local";

    private final Clock reloj = Clock.fixed(HOY.atZone(BOGOTA).toInstant(), BOGOTA);
    private final ConfiguracionAlojamientoFake configuracion = ConfiguracionAlojamientoFake.ficha();
    private final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    private final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria(reloj);
    private CodigoReserva codigo;

    @BeforeEach
    void abrirFolio() {
        Reserva reserva = FichaStaylyUq.reservaPendiente(FichaStaylyUq.suq201(), FichaStaylyUq.diciembre(1, 3));
        reservas.guardar(reserva);
        codigo = reserva.codigo();
        folios.guardar(Folio.abrir(new IdFolio(UUID.randomUUID()), codigo, Dinero.pesos(300_000), HOY));
    }

    @Test
    @DisplayName("CU-07 · Un pago parcial baja el saldo y queda con su medio, fecha y referencia")
    void registrarPagoParcial() {
        Folio folio = registrarPago().ejecutar(codigo, Dinero.pesos(90_000), MedioPago.TRANSFERENCIA, HOY, "TRF-1");

        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(210_000));
        assertThat(folio.pagos()).singleElement()
                .satisfies(pago -> assertThat(pago.referencia()).isEqualTo("TRF-1"));
        assertThat(folios.buscarPorCodigoReserva(codigo).orElseThrow().totalPagos()).isEqualTo(Dinero.pesos(90_000));
    }

    @Test
    @DisplayName("L-18 · Un medio de pago que el alojamiento no acepta es MEDIO_PAGO_NO_ACEPTADO")
    void medioNoAceptado() {
        assertThatThrownBy(() -> registrarPago().ejecutar(codigo, Dinero.pesos(90_000), MedioPago.TARJETA, HOY, null))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "MEDIO_PAGO_NO_ACEPTADO");
    }

    @Test
    @DisplayName("3.4 · Un pago de valor cero es VALOR_INVALIDO")
    void pagoEnCero() {
        assertThatThrownBy(() -> registrarPago().ejecutar(codigo, Dinero.cero(), MedioPago.EFECTIVO, HOY, null))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "VALOR_INVALIDO");
    }

    @Test
    @DisplayName("7.7 · Sin la reserva el pago es RESERVA_NO_ENCONTRADA")
    void reservaInexistente() {
        CodigoReserva otra = new CodigoReserva("RES-2026-99999");
        assertThatThrownBy(() -> registrarPago().ejecutar(otra, Dinero.pesos(1_000), MedioPago.EFECTIVO, HOY, null))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasFieldOrPropertyWithValue("codigo", "RESERVA_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("RN-16 · Un servicio adicional suma al saldo con la fecha del reloj")
    void registrarServicioAdicional() {
        Folio folio = registrarCargo().ejecutar(codigo, TipoCargo.SERVICIO_ADICIONAL, "Parqueadero", Dinero.pesos(15_000));

        assertThat(folio.saldo().valor()).isEqualTo(Dinero.pesos(315_000));
        assertThat(folio.cargos()).last().extracting(Cargo::tipo, Cargo::fecha)
                .containsExactly(TipoCargo.SERVICIO_ADICIONAL, LocalDateTime.now(reloj));
    }

    @Test
    @DisplayName("RN-16 · ALOJAMIENTO y PENALIDAD no se registran a mano")
    void cargoDelSistema() {
        assertThatThrownBy(() -> registrarCargo().ejecutar(codigo, TipoCargo.PENALIDAD, "Multa", Dinero.pesos(10_000)))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "PARAMETRO_INVALIDO");
    }

    @Test
    @DisplayName("CU-18 · Con saldo cero recepción cierra el folio sin autorización")
    void cerrarConSaldoCero() {
        registrarPago().ejecutar(codigo, Dinero.pesos(300_000), MedioPago.EFECTIVO, HOY, null);

        Folio folio = cerrar().ejecutar(codigo, RECEPCION, Optional.empty());

        assertThat(folio.estado()).isEqualTo(EstadoFolio.CERRADO);
        assertThat(folio.autorizacion()).isEmpty();
    }

    @Test
    @DisplayName("RN-17 · Con saldo y sin motivo del administrador es FOLIO_CON_SALDO_SIN_AUTORIZACION")
    void cerrarConSaldoSinAutorizacion() {
        assertThatThrownBy(() -> cerrar().ejecutar(codigo, RECEPCION, Optional.empty()))
                .isInstanceOf(ReglaDominioException.class)
                .hasFieldOrPropertyWithValue("codigo", "FOLIO_CON_SALDO_SIN_AUTORIZACION");
        assertThatThrownBy(() -> cerrar().ejecutar(codigo, ADMIN, Optional.of("  ")))
                .hasFieldOrPropertyWithValue("codigo", "FOLIO_CON_SALDO_SIN_AUTORIZACION");
    }

    @Test
    @DisplayName("RN-17 · El administrador cierra con saldo y queda su autorización con motivo")
    void cerrarConAutorizacion() {
        Folio folio = cerrar().ejecutar(codigo, ADMIN, Optional.of("Cortesía por daño en la ducha"));

        assertThat(folio.estado()).isEqualTo(EstadoFolio.CERRADO);
        assertThat(folio.autorizacion()).hasValueSatisfying(autorizacion -> {
            assertThat(autorizacion.autor()).isEqualTo(ADMIN);
            assertThat(autorizacion.motivo()).isEqualTo("Cortesía por daño en la ducha");
        });
    }

    @Test
    @DisplayName("F-5 · Un folio cerrado no admite pagos, cargos ni otro cierre: FOLIO_CERRADO")
    void folioCerrado() {
        cerrar().ejecutar(codigo, ADMIN, Optional.of("Cortesía"));

        assertThatThrownBy(() -> registrarPago().ejecutar(codigo, Dinero.pesos(1_000), MedioPago.EFECTIVO, HOY, null))
                .hasFieldOrPropertyWithValue("codigo", "FOLIO_CERRADO");
        assertThatThrownBy(() -> registrarCargo().ejecutar(codigo, TipoCargo.AJUSTE, "Ajuste", Dinero.pesos(1_000)))
                .hasFieldOrPropertyWithValue("codigo", "FOLIO_CERRADO");
        assertThatThrownBy(() -> cerrar().ejecutar(codigo, ADMIN, Optional.of("Otra vez")))
                .hasFieldOrPropertyWithValue("codigo", "FOLIO_CERRADO");
    }

    private RegistrarPagoFolioUseCase registrarPago() {
        return new RegistrarPagoFolioUseCase(reservas, folios, configuracion);
    }

    private RegistrarCargoFolioUseCase registrarCargo() {
        return new RegistrarCargoFolioUseCase(reservas, folios, reloj);
    }

    private CerrarFolioUseCase cerrar() {
        return new CerrarFolioUseCase(reservas, folios, reloj);
    }
}

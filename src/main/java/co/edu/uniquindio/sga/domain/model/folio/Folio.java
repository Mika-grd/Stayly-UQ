package co.edu.uniquindio.sga.domain.model.folio;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Folio: cargos, pagos y saldo de una sola reserva (referenciada por
 * CodigoReserva). Invariantes F-1 a F-5.
 */
public class Folio {

    private final IdFolio id;
    private final CodigoReserva reserva;
    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private EstadoFolio estado;
    private AutorizacionCierre autorizacion;

    private Folio(IdFolio id, CodigoReserva reserva) {
        this.id = id;
        this.reserva = reserva;
        this.estado = EstadoFolio.ABIERTO;
    }

    /** 7.7: se abre al crear la reserva, con el cargo de alojamiento. */
    public static Folio abrir(IdFolio id, CodigoReserva reserva, Dinero valorAlojamiento, LocalDateTime ahora) {
        if (id == null || reserva == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El folio requiere identificador y reserva.");
        }
        if (valorAlojamiento == null || !valorAlojamiento.esPositivo()) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        Folio folio = new Folio(id, reserva);
        folio.cargos.add(new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento", valorAlojamiento, ahora));
        return folio;
    }

    public void registrarCargo(Cargo cargo) {
        verificarAbierto();
        if (cargo == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El cargo es obligatorio.");
        }
        cargos.add(cargo);
    }

    public void registrarPago(Pago pago) {
        verificarAbierto();
        if (pago == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El pago es obligatorio.");
        }
        pagos.add(pago);
    }

    /** RN-14: deja la diferencia de una modificación (positiva o negativa). Sin diferencia no hay movimiento. */
    public void registrarAjuste(Dinero diferencia, LocalDateTime ahora) {
        verificarAbierto();
        if (diferencia.redondear().esCero()) {
            return;
        }
        cargos.add(new Cargo(TipoCargo.AJUSTE, "Ajuste por modificación de la reserva", diferencia, ahora));
    }

    /**
     * 7.5: anula el alojamiento (cargo original y ajustes) con movimientos inversos y registra
     * la retención como penalidad. Lo pagado de más queda como saldo a favor.
     */
    public void liquidarCancelacion(Dinero retencion, LocalDateTime ahora) {
        verificarAbierto();
        if (retencion == null || (!retencion.esPositivo() && !retencion.esCero())) {
            throw new ReglaDominioException("VALOR_INVALIDO", "El valor del movimiento no es válido.");
        }
        List<Cargo> anulaciones = cargos.stream()
                .filter(cargo -> cargo.tipo() == TipoCargo.ALOJAMIENTO || cargo.tipo() == TipoCargo.AJUSTE)
                .map(cargo -> cargo.inverso(ahora))
                .toList();
        cargos.addAll(anulaciones);
        if (retencion.esPositivo()) {
            cargos.add(new Cargo(TipoCargo.PENALIDAD, "Retención según la política de cancelación", retencion, ahora));
        }
    }

    /** F-1: se calcula cada vez, nunca se guarda. */
    public Saldo saldo() {
        return new Saldo(totalCargos().restar(totalPagos()));
    }

    public Dinero totalCargos() {
        return cargos.stream().map(Cargo::valor).reduce(Dinero.cero(), Dinero::sumar);
    }

    public Dinero totalPagos() {
        return pagos.stream().map(Pago::valor).reduce(Dinero.cero(), Dinero::sumar);
    }

    /** F-3 / RN-17: con saldo distinto de cero se exige {@link #cerrarConAutorizacion}. */
    public void cerrar(LocalDateTime ahora) {
        verificarAbierto();
        if (!saldo().esCero()) {
            throw new ReglaDominioException("FOLIO_CON_SALDO_SIN_AUTORIZACION",
                    "El folio tiene saldo pendiente; se requiere autorización del administrador.");
        }
        estado = EstadoFolio.CERRADO;
    }

    public void cerrarConAutorizacion(AutorizacionCierre autorizacion) {
        verificarAbierto();
        if (autorizacion == null) {
            throw new ReglaDominioException("FOLIO_CON_SALDO_SIN_AUTORIZACION",
                    "El folio tiene saldo pendiente; se requiere autorización del administrador.");
        }
        this.autorizacion = autorizacion;
        estado = EstadoFolio.CERRADO;
    }

    public boolean estaCerrado() {
        return estado == EstadoFolio.CERRADO;
    }

    /** F-2: listas inmodificables; solo crecen por registrarCargo / registrarPago. */
    public List<Cargo> cargos() {
        return Collections.unmodifiableList(cargos);
    }

    public List<Pago> pagos() {
        return Collections.unmodifiableList(pagos);
    }

    /** F-5: un folio cerrado no admite nuevos movimientos. */
    private void verificarAbierto() {
        if (estaCerrado()) {
            throw new ReglaDominioException("FOLIO_CERRADO", "El folio está cerrado y no admite movimientos.");
        }
    }

    public IdFolio id() { return id; }
    public CodigoReserva reserva() { return reserva; }
    public EstadoFolio estado() { return estado; }
    public Optional<AutorizacionCierre> autorizacion() { return Optional.ofNullable(autorizacion); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Folio otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    void guardar(Reserva reserva);

    Optional<Reserva> buscarPorCodigo(CodigoReserva codigo);

    /** Solo reservas que retienen disponibilidad (RN-12) y comparten al menos una noche con la estancia. */
    List<Reserva> buscarActivasQueSolapan(IdentificacionApartamento apartamento, Estancia estancia);

    Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador);

    List<Reserva> buscarPendientesCreadasAntesDe(LocalDateTime limite);

    /** Reservas activas o con salida posterior a {@code hoy} (7.3). */
    boolean tieneActivasOFuturas(IdentificacionApartamento apartamento, LocalDate hoy);

    CodigoReserva siguienteCodigo();
}

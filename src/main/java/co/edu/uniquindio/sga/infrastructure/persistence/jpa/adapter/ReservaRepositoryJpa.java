package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConsecutivoReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ConsecutivoReservaJpaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Adaptador JPA del puerto ReservaRepository. */
public class ReservaRepositoryJpa implements ReservaRepository {

    /** RN-12: solo las que retienen disponibilidad (PENDIENTE, CONFIRMADA, EN_CURSO). */
    private static final Set<EstadoReserva> ACTIVAS = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::retieneDisponibilidad)
            .collect(Collectors.toUnmodifiableSet());

    private final ReservaJpaRepository jpa;
    private final ConsecutivoReservaJpaRepository consecutivos;
    private final Clock reloj;

    public ReservaRepositoryJpa(ReservaJpaRepository jpa, ConsecutivoReservaJpaRepository consecutivos, Clock reloj) {
        this.jpa = jpa;
        this.consecutivos = consecutivos;
        this.reloj = reloj;
    }

    @Override
    public void guardar(Reserva reserva) {
        jpa.save(ReservaMapper.aEntidad(reserva));
    }

    @Override
    public Optional<Reserva> buscarPorCodigo(CodigoReserva codigo) {
        return jpa.findById(codigo.valor()).map(ReservaMapper::aDominio);
    }

    @Override
    public List<Reserva> buscarActivasQueSolapan(IdentificacionApartamento apartamento, Estancia estancia) {
        return jpa.buscarQueSolapan(apartamento.valor(), ACTIVAS, estancia.entrada(), estancia.salida()).stream()
                .map(ReservaMapper::aDominio)
                .toList();
    }

    @Override
    public Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador) {
        return jpa.findByIdentificadorExternoCanalAndIdentificadorExternoCodigo(identificador.canal().valor(),
                identificador.codigo()).map(ReservaMapper::aDominio);
    }

    @Override
    public List<Reserva> buscarPendientesCreadasAntesDe(LocalDateTime limite) {
        return jpa.findByEstadoAndFechaCreacionBefore(EstadoReserva.PENDIENTE, limite).stream()
                .map(ReservaMapper::aDominio)
                .toList();
    }

    @Override
    public boolean tieneActivasOFuturas(IdentificacionApartamento apartamento, LocalDate hoy) {
        return jpa.existsByApartamentoAndEstadoInAndEstanciaSalidaAfter(apartamento.valor(), ACTIVAS, hoy);
    }

    /** RES-AAAA-NNNNN: el número se reinicia cada año y se toma con la fila del año bloqueada. */
    @Override
    @Transactional
    public CodigoReserva siguienteCodigo() {
        int anio = Year.now(reloj).getValue();
        ConsecutivoReservaEntity consecutivo = consecutivos.bloquearAnio(anio)
                .orElseGet(() -> consecutivos.save(new ConsecutivoReservaEntity(anio)));
        int numero = consecutivo.siguiente();
        return new CodigoReserva("RES-%d-%05d".formatted(anio, numero));
    }
}

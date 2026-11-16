package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 7.3: reemplaza los datos descriptivos. Cambiar la capacidad no afecta las reservas ya creadas:
 * se devuelven las que quedan por encima de la nueva capacidad para advertir al administrador.
 */
@Service
@Transactional
public class ActualizarApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;
    private final Clock reloj;

    public ActualizarApartamentoUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas, Clock reloj) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.reloj = reloj;
    }

    public ApartamentoActualizado ejecutar(IdentificacionApartamento identificacion, DatosApartamento datos) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        apartamento.actualizarDatos(datos.nombre(), datos.descripcion(), datos.dormitorios(), datos.capacidad(),
                datos.admiteMascotas(), datos.advertenciaAcceso(), datos.caracteristicas(), datos.imagenes());
        apartamentos.guardar(apartamento);
        List<CodigoReserva> sobreNuevaCapacidad = reservas.buscarActivasOFuturas(identificacion,
                        LocalDate.now(reloj)).stream()
                .filter(reserva -> !apartamento.admite(reserva.totalOcupantes()))
                .map(Reserva::codigo)
                .toList();
        return new ApartamentoActualizado(apartamento, sobreNuevaCapacidad);
    }

    public record ApartamentoActualizado(Apartamento apartamento, List<CodigoReserva> reservasSobreNuevaCapacidad) {
    }
}

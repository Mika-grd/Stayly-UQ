package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.util.List;

/**
 * RN-01, RN-02, RN-07, RN-12, RN-20 y 3.3. Compara contra todas las reservas activas y los
 * bloqueos del apartamento: ninguna reserva conoce a las demás.
 */
public class DisponibilidadApartamentoService {

    private final ReservaRepository reservas;
    private final BloqueoRepository bloqueos;
    private final ConfiguracionAlojamiento configuracion;

    public DisponibilidadApartamentoService(ReservaRepository reservas, BloqueoRepository bloqueos,
                                            ConfiguracionAlojamiento configuracion) {
        this.reservas = reservas;
        this.bloqueos = bloqueos;
        this.configuracion = configuracion;
    }

    /**
     * Lanza ReglaDominioException si el apartamento no se puede vender para esa estancia y ese
     * grupo. {@code reservaExcluida} permite revalidar una modificación sin chocar consigo misma
     * (null al crear).
     */
    public void verificarDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                    CodigoReserva reservaExcluida) {
        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException("APARTAMENTO_INACTIVO", "El apartamento no está disponible para la venta.");
        }
        if (!apartamento.admite(totalOcupantes)) {
            throw new ReglaDominioException("CAPACIDAD_EXCEDIDA",
                    "El número de ocupantes excede la capacidad del apartamento.");
        }
        boolean haySolapamiento = reservas.buscarActivasQueSolapan(apartamento.identificacion(), estancia).stream()
                .anyMatch(reserva -> !reserva.codigo().equals(reservaExcluida));
        if (haySolapamiento) {
            throw new ReglaDominioException("NOCHES_NO_DISPONIBLES",
                    "El apartamento ya tiene una reserva activa que solapa esas noches.");
        }
        if (bloqueos.buscarVigentesQueSolapan(apartamento.identificacion(), estancia).stream()
                .anyMatch(bloqueo -> bloqueo.afecta(estancia))) {
            throw new ReglaDominioException("APARTAMENTO_BLOQUEADO",
                    "El apartamento está bloqueado en alguna de esas noches.");
        }
        verificarTiempoPreparacion(apartamento, estancia, reservaExcluida);
    }

    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes) {
        try {
            verificarDisponible(apartamento, estancia, totalOcupantes, null);
            return true;
        } catch (ReglaDominioException e) {
            return false;
        }
    }

    /**
     * RN-20: si el tiempo de preparación no cabe entre la hora de salida y la de entrada, no se
     * puede entrar el mismo día en que sale otro grupo, ni salir el día en que otro entra.
     */
    private void verificarTiempoPreparacion(Apartamento apartamento, Estancia estancia,
                                            CodigoReserva reservaExcluida) {
        if (configuracion.tiempoPreparacion().cabeEn(configuracion.horario())) {
            return;
        }
        Estancia conDiasVecinos = new Estancia(estancia.entrada().minusDays(1), estancia.salida().plusDays(1));
        List<Reserva> vecinas = reservas.buscarActivasQueSolapan(apartamento.identificacion(), conDiasVecinos);
        boolean hayCambioDeGrupoElMismoDia = vecinas.stream()
                .filter(reserva -> !reserva.codigo().equals(reservaExcluida))
                .anyMatch(reserva -> reserva.estancia().salida().equals(estancia.entrada())
                        || reserva.estancia().entrada().equals(estancia.salida()));
        if (hayCambioDeGrupoElMismoDia) {
            throw new ReglaDominioException("TIEMPO_PREPARACION_INSUFICIENTE",
                    "No hay tiempo de preparación suficiente después de la salida anterior.");
        }
    }
}

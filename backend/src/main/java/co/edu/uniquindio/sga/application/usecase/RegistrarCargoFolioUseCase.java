package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Cargo manual del folio (7.7, RN-16): solo SERVICIO_ADICIONAL o AJUSTE. El alojamiento y la penalidad
 * los registra el sistema al crear, modificar o cancelar la reserva.
 */
@Service
@Transactional
public class RegistrarCargoFolioUseCase {

    private static final Set<TipoCargo> MANUALES = Set.of(TipoCargo.SERVICIO_ADICIONAL, TipoCargo.AJUSTE);

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final Clock reloj;

    public RegistrarCargoFolioUseCase(ReservaRepository reservas, FolioRepository folios, Clock reloj) {
        this.reservas = reservas;
        this.folios = folios;
        this.reloj = reloj;
    }

    public Folio ejecutar(CodigoReserva codigo, TipoCargo tipo, String concepto, Dinero valor) {
        if (!MANUALES.contains(tipo)) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "Solo se registran a mano cargos SERVICIO_ADICIONAL o AJUSTE.");
        }
        Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        folio.registrarCargo(new Cargo(tipo, concepto, valor, LocalDateTime.now(reloj)));
        folios.guardar(folio);
        return folio;
    }
}

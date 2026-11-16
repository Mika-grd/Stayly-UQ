package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El folio de una reserva, con su saldo (F-8). Lo usa ReservaResponse para mostrar saldoFolio. */
@Service
@Transactional(readOnly = true)
public class ConsultarFolioUseCase {

    private final FolioRepository folios;

    public ConsultarFolioUseCase(FolioRepository folios) {
        this.folios = folios;
    }

    public Folio ejecutar(CodigoReserva codigo) {
        return Consultas.folio(folios, codigo);
    }
}

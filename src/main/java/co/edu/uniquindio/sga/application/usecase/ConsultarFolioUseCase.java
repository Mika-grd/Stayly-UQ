package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.7: el folio de una reserva, para mostrar su saldo junto al detalle de la reserva. */
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

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * CU-07 (7.7): abona al folio con su medio y su fecha (RN-15). Admite pagos parciales y, para corregir,
 * un valor negativo (RN-16). El medio debe ser uno de los que acepta el alojamiento (L-18).
 */
@Service
@Transactional
public class RegistrarPagoFolioUseCase {

    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final ConfiguracionAlojamiento configuracion;

    public RegistrarPagoFolioUseCase(ReservaRepository reservas, FolioRepository folios,
                                     ConfiguracionAlojamiento configuracion) {
        this.reservas = reservas;
        this.folios = folios;
        this.configuracion = configuracion;
    }

    public Folio ejecutar(CodigoReserva codigo, Dinero valor, MedioPago medio, LocalDateTime fecha, String referencia) {
        Consultas.reserva(reservas, codigo);
        Folio folio = Consultas.folio(folios, codigo);
        if (medio != null && !configuracion.alojamiento().aceptaMedioPago(medio)) {
            throw new ReglaDominioException("MEDIO_PAGO_NO_ACEPTADO",
                    "El medio de pago no es aceptado por el alojamiento.");
        }
        folio.registrarPago(new Pago(valor, medio, fecha, referencia));
        folios.guardar(folio);
        return folio;
    }
}

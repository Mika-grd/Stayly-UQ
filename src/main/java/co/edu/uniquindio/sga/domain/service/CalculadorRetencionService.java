package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.politica.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * RN-13 y 7.5: cruza la antelación de la cancelación con la versión de política congelada en
 * la reserva, no con la vigente. El caso de uso entrega el resultado a Folio.liquidarCancelacion.
 */
public class CalculadorRetencionService {

    private final PoliticaCancelacionRepository politicas;

    public CalculadorRetencionService(PoliticaCancelacionRepository politicas) {
        this.politicas = politicas;
    }

    /** La antelación son los días calendario entre la cancelación y la fecha de entrada. */
    public Dinero calcularRetencion(Reserva reserva, LocalDateTime momentoCancelacion) {
        long diasAntelacion = ChronoUnit.DAYS.between(momentoCancelacion.toLocalDate(), reserva.estancia().entrada());
        return politicaDe(reserva).retencionPara(reserva.valor().total(), diasAntelacion);
    }

    public Dinero calcularPenalidadNoShow(Reserva reserva) {
        return politicaDe(reserva).retencionNoShow(reserva.valor().total());
    }

    private PoliticaCancelacion politicaDe(Reserva reserva) {
        return politicas.buscarPorVersion(reserva.versionPolitica());
    }
}

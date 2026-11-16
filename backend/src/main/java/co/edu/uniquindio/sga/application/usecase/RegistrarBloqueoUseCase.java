package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.service.RegistroBloqueoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-16: RegistroBloqueoService rechaza el bloqueo si alguna noche del rango tiene reservas
 * activas (7.3). Un bloqueo no cambia el estado operativo del apartamento (3.3).
 */
@Service
@Transactional
public class RegistrarBloqueoUseCase {

    private final ApartamentoRepository apartamentos;
    private final BloqueoRepository bloqueos;
    private final RegistroBloqueoService registro;
    private final Clock reloj;

    public RegistrarBloqueoUseCase(ApartamentoRepository apartamentos, BloqueoRepository bloqueos,
                                   RegistroBloqueoService registro, Clock reloj) {
        this.apartamentos = apartamentos;
        this.bloqueos = bloqueos;
        this.registro = registro;
        this.reloj = reloj;
    }

    public Bloqueo ejecutar(IdentificacionApartamento apartamento, RangoFechas rango, String motivo, String autor) {
        Consultas.apartamento(apartamentos, apartamento);
        Bloqueo bloqueo = registro.registrar(apartamento, rango, motivo, autor, LocalDateTime.now(reloj));
        bloqueos.guardar(bloqueo);
        return bloqueo;
    }
}

package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.bloqueo.Bloqueo;
import co.edu.uniquindio.sga.domain.model.bloqueo.IdBloqueo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/** 7.3 y RN-07: no se registra un bloqueo sobre noches con reservas activas (otro agregado). */
public class RegistroBloqueoService {

    private final ReservaRepository reservas;

    public RegistroBloqueoService(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public Bloqueo registrar(IdentificacionApartamento apartamento, RangoFechas rango, String motivo, String autor,
                             LocalDateTime ahora) {
        Estancia nochesBloqueadas = new Estancia(rango.inicio(), rango.fin());
        if (!reservas.buscarActivasQueSolapan(apartamento, nochesBloqueadas).isEmpty()) {
            throw new ReglaDominioException("BLOQUEO_SOBRE_RESERVAS_ACTIVAS",
                    "No se puede bloquear: hay reservas activas en esas noches.");
        }
        return Bloqueo.registrar(new IdBloqueo(UUID.randomUUID()), apartamento, rango, motivo, autor, ahora);
    }
}

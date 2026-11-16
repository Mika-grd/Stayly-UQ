package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.novedad.GravedadNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.IdNovedad;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CU-15 (7.6): Apartamento.declararFueraDeServicio desde cualquier estado no ocupado (A-3). El motivo
 * queda como una novedad de gravedad ALTA. No registra un bloqueo por sí solo (3.3): el administrador
 * lo hace con CU-16.
 */
@Service
@Transactional
public class DeclararFueraDeServicioUseCase {

    private final ApartamentoRepository apartamentos;
    private final NovedadRepository novedades;
    private final Clock reloj;

    public DeclararFueraDeServicioUseCase(ApartamentoRepository apartamentos, NovedadRepository novedades,
                                          Clock reloj) {
        this.apartamentos = apartamentos;
        this.novedades = novedades;
        this.reloj = reloj;
    }

    public CambioEstadoOperativo ejecutar(IdentificacionApartamento identificacion, String motivo, String autor) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        apartamento.declararFueraDeServicio();
        Novedad novedad = Novedad.registrar(new IdNovedad(UUID.randomUUID()), identificacion, autor,
                "Fuera de servicio: " + motivo, GravedadNovedad.ALTA, ahora);
        apartamentos.guardar(apartamento);
        novedades.guardar(novedad);
        return new CambioEstadoOperativo(apartamento, autor, ahora);
    }
}

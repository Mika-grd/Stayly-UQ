package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.temporada.EstanciaMinima;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 7.4: crear, reemplazar y listar temporadas. Temporada protege T-1 (periodos propios sin
 * solaparse; la base sin periodos) y CalendarioTemporadasService que no se solape con otras y que
 * haya una sola base (F-05).
 */
@Service
@Transactional
public class GestionarTemporadasUseCase {

    private final TemporadaRepository temporadas;
    private final CalendarioTemporadasService calendario;

    public GestionarTemporadasUseCase(TemporadaRepository temporadas, CalendarioTemporadasService calendario) {
        this.temporadas = temporadas;
        this.calendario = calendario;
    }

    public Temporada crear(DatosTemporada datos) {
        return registrar(new IdTemporada(UUID.randomUUID()), datos);
    }

    public Temporada actualizar(IdTemporada id, DatosTemporada datos) {
        Consultas.temporada(temporadas, id);
        return registrar(id, datos);
    }

    @Transactional(readOnly = true)
    public List<Temporada> listar() {
        return temporadas.listarTodas();
    }

    private Temporada registrar(IdTemporada id, DatosTemporada datos) {
        Temporada temporada = construir(id, datos);
        calendario.verificarCalendario(temporada);
        temporadas.guardar(temporada);
        return temporada;
    }

    /** Sin estancia mínima se usa 1 noche (RP-01). Un periodo en la base lo rechaza la propia Temporada. */
    private static Temporada construir(IdTemporada id, DatosTemporada datos) {
        EstanciaMinima estanciaMinima = datos.estanciaMinima() == null ? new EstanciaMinima(1) : datos.estanciaMinima();
        List<RangoFechas> periodos = datos.periodos() == null ? List.of() : datos.periodos();
        if (!datos.esBase()) {
            return Temporada.crear(id, datos.nombre(), periodos, estanciaMinima);
        }
        Temporada base = Temporada.crearBase(id, datos.nombre());
        periodos.forEach(base::agregarPeriodo);
        base.cambiarEstanciaMinima(estanciaMinima);
        return base;
    }

    public record DatosTemporada(String nombre, List<RangoFechas> periodos, boolean esBase,
                                 EstanciaMinima estanciaMinima) {
    }
}

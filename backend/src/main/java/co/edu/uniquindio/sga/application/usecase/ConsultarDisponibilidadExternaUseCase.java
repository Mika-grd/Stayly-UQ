package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.TipoEventoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 10.3.1: el canal pregunta qué apartamentos puede vender para la estancia y el grupo. Es la misma
 * búsqueda de CU-20, sin filtros y sin paginar; la consulta queda en la bitácora del canal.
 */
@Service
@Transactional
public class ConsultarDisponibilidadExternaUseCase {

    private final CanalRepository canales;
    private final BuscarApartamentosDisponiblesUseCase buscar;
    private final Clock reloj;

    public ConsultarDisponibilidadExternaUseCase(CanalRepository canales, BuscarApartamentosDisponiblesUseCase buscar,
                                                 Clock reloj) {
        this.canales = canales;
        this.buscar = buscar;
        this.reloj = reloj;
    }

    public List<BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible> ejecutar(
            IdCanal idCanal, LocalDate entrada, LocalDate salida, List<LocalDate> fechasNacimiento) {
        Canal canal = Consultas.canal(canales, idCanal);
        var criterios = new BuscarApartamentosDisponiblesUseCase.Criterios(entrada, salida, fechasNacimiento,
                null, null, null);
        List<BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible> disponibles = new ArrayList<>();
        Pagina<BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible> pagina;
        int numero = 0;
        do {
            pagina = buscar.ejecutar(criterios, new PeticionPagina(numero++));
            disponibles.addAll(pagina.contenido());
        } while (numero < pagina.totalPaginas());

        canal.registrarEvento(new EventoCanal(TipoEventoCanal.DISPONIBILIDAD_CONSULTADA.name(), null,
                LocalDateTime.now(reloj), disponibles.size() + " apartamentos disponibles del " + entrada
                + " al " + salida));
        canales.guardar(canal);
        return disponibles;
    }
}

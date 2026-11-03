package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * CU-20: los apartamentos activos que se pueden vender para la estancia y el grupo, con su valor
 * estimado, de a 10 (7.5). La disponibilidad la decide DisponibilidadApartamentoService (RN-01,
 * RN-02, RN-07, RN-20) y el valor CotizadorEstanciaService. Capacidad mínima, precio máximo y
 * características son filtros de búsqueda, no reglas del negocio.
 */
@Service
@Transactional(readOnly = true)
public class BuscarApartamentosDisponiblesUseCase {

    private final ApartamentoRepository apartamentos;
    private final DisponibilidadApartamentoService disponibilidad;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final CotizadorEstanciaService cotizador;
    private final Clock reloj;

    public BuscarApartamentosDisponiblesUseCase(ApartamentoRepository apartamentos,
                                                DisponibilidadApartamentoService disponibilidad,
                                                VerificadorTarifasCompletasService verificadorTarifas,
                                                CotizadorEstanciaService cotizador, Clock reloj) {
        this.apartamentos = apartamentos;
        this.disponibilidad = disponibilidad;
        this.verificadorTarifas = verificadorTarifas;
        this.cotizador = cotizador;
        this.reloj = reloj;
    }

    public Pagina<ApartamentoDisponible> ejecutar(Criterios criterios, PeticionPagina pagina) {
        Estancia estancia = new Estancia(criterios.entrada(), criterios.salida());
        estancia.verificarEntradaNoPasada(LocalDate.now(reloj));
        ComposicionGrupo grupo = new ComposicionGrupo(criterios.fechasNacimiento());

        List<ApartamentoDisponible> disponibles = apartamentos.listarActivos().stream()
                .filter(apartamento -> criterios.capacidadMinima() == null
                        || apartamento.capacidad().personas() >= criterios.capacidadMinima())
                .filter(apartamento -> criterios.caracteristicas() == null
                        || apartamento.caracteristicas().containsAll(criterios.caracteristicas()))
                .filter(apartamento -> verificadorTarifas.tieneTarifasCompletas(apartamento.identificacion()))
                .filter(apartamento -> disponibilidad.estaDisponible(apartamento, estancia, grupo.total()))
                .map(apartamento -> new ApartamentoDisponible(apartamento,
                        cotizador.cotizar(apartamento.identificacion(), estancia, grupo)))
                .filter(disponible -> criterios.precioMaximo() == null
                        || !disponible.valorEstimado().total().restar(criterios.precioMaximo()).esPositivo())
                .sorted(Comparator.comparing(disponible -> disponible.apartamento().identificacion().valor()))
                .toList();
        return Pagina.de(disponibles, pagina);
    }

    /** Filtros de la búsqueda; los opcionales en null no filtran. */
    public record Criterios(LocalDate entrada, LocalDate salida, List<LocalDate> fechasNacimiento,
                            Integer capacidadMinima, Dinero precioMaximo, Set<Caracteristica> caracteristicas) {
    }

    public record ApartamentoDisponible(Apartamento apartamento, ValorCongelado valorEstimado) {
    }
}

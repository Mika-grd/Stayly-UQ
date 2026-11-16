package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

/**
 * 7.3: el administrador gestiona el inventario completo, también los apartamentos inactivos o
 * retirados (que la búsqueda ignora). Cada uno lleva si tiene tarifas completas, la condición para
 * activarlo (A-5). De a 10 por página (12.4), ordenados por identificación.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarApartamentosUseCase {

    private final ApartamentoRepository apartamentos;
    private final VerificadorTarifasCompletasService verificadorTarifas;

    public ConsultarApartamentosUseCase(ApartamentoRepository apartamentos,
                                        VerificadorTarifasCompletasService verificadorTarifas) {
        this.apartamentos = apartamentos;
        this.verificadorTarifas = verificadorTarifas;
    }

    public Pagina<ApartamentoInventario> ejecutar(PeticionPagina pagina) {
        return Pagina.de(apartamentos.listarTodos().stream()
                .sorted(Comparator.comparing(apartamento -> apartamento.identificacion().valor()))
                .map(apartamento -> new ApartamentoInventario(apartamento,
                        verificadorTarifas.tieneTarifasCompletas(apartamento.identificacion())))
                .toList(), pagina);
    }

    public record ApartamentoInventario(Apartamento apartamento, boolean tarifasCompletas) {
    }
}

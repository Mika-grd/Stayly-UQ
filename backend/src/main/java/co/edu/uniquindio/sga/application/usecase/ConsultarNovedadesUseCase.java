package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.novedad.Novedad;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CU-41 (7.6): novedades de un apartamento, de la más reciente a la más antigua, de a 10. */
@Service
@Transactional(readOnly = true)
public class ConsultarNovedadesUseCase {

    private final ApartamentoRepository apartamentos;
    private final NovedadRepository novedades;

    public ConsultarNovedadesUseCase(ApartamentoRepository apartamentos, NovedadRepository novedades) {
        this.apartamentos = apartamentos;
        this.novedades = novedades;
    }

    public Pagina<Novedad> ejecutar(IdentificacionApartamento apartamento, PeticionPagina pagina) {
        Consultas.apartamento(apartamentos, apartamento);
        return novedades.buscarPorApartamento(apartamento, pagina);
    }
}

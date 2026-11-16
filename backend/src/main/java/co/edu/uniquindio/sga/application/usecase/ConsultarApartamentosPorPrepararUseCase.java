package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.EstadoOperativo;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** CU-43 (7.1): el personal de servicio consulta los apartamentos pendientes o en preparación. */
@Service
@Transactional(readOnly = true)
public class ConsultarApartamentosPorPrepararUseCase {

    private static final Set<EstadoOperativo> POR_PREPARAR =
            Set.of(EstadoOperativo.PENDIENTE_PREPARACION, EstadoOperativo.EN_PREPARACION);

    private final ApartamentoRepository apartamentos;

    public ConsultarApartamentosPorPrepararUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = apartamentos;
    }

    public List<Apartamento> ejecutar() {
        return apartamentos.listarActivos().stream()
                .filter(apartamento -> POR_PREPARAR.contains(apartamento.estadoOperativo()))
                .sorted(Comparator.comparing(apartamento -> apartamento.identificacion().valor()))
                .toList();
    }
}

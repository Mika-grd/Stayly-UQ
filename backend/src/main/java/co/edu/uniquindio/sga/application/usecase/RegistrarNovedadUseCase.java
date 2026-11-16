package co.edu.uniquindio.sga.application.usecase;

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
 * CU-11 (7.6): registra un daño, faltante o situación con fecha, autor, descripción y gravedad. No
 * cambia el estado operativo (3.3).
 */
@Service
@Transactional
public class RegistrarNovedadUseCase {

    private final ApartamentoRepository apartamentos;
    private final NovedadRepository novedades;
    private final Clock reloj;

    public RegistrarNovedadUseCase(ApartamentoRepository apartamentos, NovedadRepository novedades, Clock reloj) {
        this.apartamentos = apartamentos;
        this.novedades = novedades;
        this.reloj = reloj;
    }

    public Novedad ejecutar(IdentificacionApartamento apartamento, String descripcion, GravedadNovedad gravedad,
                            String autor) {
        Consultas.apartamento(apartamentos, apartamento);
        Novedad novedad = Novedad.registrar(new IdNovedad(UUID.randomUUID()), apartamento, autor, descripcion,
                gravedad, LocalDateTime.now(reloj));
        novedades.guardar(novedad);
        return novedad;
    }
}

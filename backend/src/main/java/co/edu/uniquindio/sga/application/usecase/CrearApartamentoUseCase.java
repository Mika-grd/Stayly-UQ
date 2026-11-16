package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.AltaApartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 7.3: registra un apartamento. AltaApartamentoService verifica que la identificación no esté
 * usada (L-04) y Apartamento.crear valida las imágenes (A-4). Nace inactivo hasta tener tarifas.
 */
@Service
@Transactional
public class CrearApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final AltaApartamentoService alta;

    public CrearApartamentoUseCase(ApartamentoRepository apartamentos, AltaApartamentoService alta) {
        this.apartamentos = apartamentos;
        this.alta = alta;
    }

    public Apartamento ejecutar(IdentificacionApartamento identificacion, DatosApartamento datos) {
        Apartamento apartamento = alta.darDeAlta(identificacion, datos.nombre(), datos.descripcion(),
                datos.dormitorios(), datos.capacidad(), datos.admiteMascotas(), datos.advertenciaAcceso(),
                datos.caracteristicas(), datos.imagenes());
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}

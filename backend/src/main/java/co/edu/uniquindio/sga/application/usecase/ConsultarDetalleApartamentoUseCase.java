package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.alojamiento.Ubicacion;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.1: detalle completo de un apartamento, con la ubicación del alojamiento para el mapa. */
@Service
@Transactional(readOnly = true)
public class ConsultarDetalleApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final ConfiguracionAlojamiento configuracion;

    public ConsultarDetalleApartamentoUseCase(ApartamentoRepository apartamentos,
                                              ConfiguracionAlojamiento configuracion) {
        this.apartamentos = apartamentos;
        this.configuracion = configuracion;
    }

    public DetalleApartamento ejecutar(IdentificacionApartamento identificacion) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        return new DetalleApartamento(apartamento, configuracion.alojamiento().ubicacion());
    }

    public record DetalleApartamento(Apartamento apartamento, Ubicacion ubicacion) {
    }
}

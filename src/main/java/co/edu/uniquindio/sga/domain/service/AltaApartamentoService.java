package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;

import java.util.List;
import java.util.Set;

/**
 * L-04: la identificación del apartamento es única en todo el alojamiento. Un Apartamento no puede
 * saber si otro ya usa su identificación, por eso la verifica este servicio contra el repositorio
 * (incluye los retirados: la identificación es estable y las reservas históricas la siguen usando).
 */
public class AltaApartamentoService {

    private final ApartamentoRepository apartamentos;

    public AltaApartamentoService(ApartamentoRepository apartamentos) {
        this.apartamentos = apartamentos;
    }

    /** Nace inactivo y en PENDIENTE_PREPARACION: se activa cuando tenga tarifas completas (7.4). */
    public Apartamento darDeAlta(IdentificacionApartamento identificacion, String nombre, String descripcion,
                                 int dormitorios, Capacidad capacidad, boolean admiteMascotas,
                                 String advertenciaAcceso, Set<Caracteristica> caracteristicas,
                                 List<ImagenApartamento> imagenes) {
        if (identificacion != null && apartamentos.buscarPorIdentificacion(identificacion).isPresent()) {
            throw new ReglaDominioException("IDENTIFICACION_DUPLICADA",
                    "Ya existe un apartamento con la identificación " + identificacion + ".");
        }
        return Apartamento.crear(identificacion, nombre, descripcion, dormitorios, capacidad, admiteMascotas,
                advertenciaAcceso, caracteristicas, imagenes);
    }
}

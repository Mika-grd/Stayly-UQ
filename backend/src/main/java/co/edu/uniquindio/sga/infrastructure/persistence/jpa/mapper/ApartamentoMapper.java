package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.ImagenApartamentoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;

import java.util.stream.Collectors;

/** Traduce entre el agregado Apartamento y su tabla. Sin lógica de negocio. */
public final class ApartamentoMapper {

    private ApartamentoMapper() {
    }

    public static ApartamentoEntity aEntidad(Apartamento apartamento) {
        return new ApartamentoEntity(apartamento.identificacion().valor(), apartamento.nombre(),
                apartamento.descripcion(), apartamento.dormitorios(), apartamento.capacidad().personas(),
                apartamento.admiteMascotas(), apartamento.advertenciaAcceso().orElse(null),
                apartamento.caracteristicas().stream().map(Caracteristica::nombre).collect(Collectors.toSet()),
                apartamento.imagenes().stream()
                        .map(imagen -> new ImagenApartamentoEmbeddable(imagen.url(), imagen.principal()))
                        .toList(),
                apartamento.estadoOperativo(), apartamento.estaActivo());
    }

    public static Apartamento aDominio(ApartamentoEntity entidad) {
        return Apartamento.reconstituir(new IdentificacionApartamento(entidad.getIdentificacion()),
                entidad.getNombre(), entidad.getDescripcion(), entidad.getDormitorios(),
                new Capacidad(entidad.getCapacidad()), entidad.isAdmiteMascotas(), entidad.getAdvertenciaAcceso(),
                entidad.getCaracteristicas().stream().map(Caracteristica::new).collect(Collectors.toSet()),
                entidad.getImagenes().stream()
                        .map(imagen -> new ImagenApartamento(imagen.getUrl(), imagen.isPrincipal()))
                        .toList(),
                entidad.getEstadoOperativo(), entidad.isActivo());
    }
}

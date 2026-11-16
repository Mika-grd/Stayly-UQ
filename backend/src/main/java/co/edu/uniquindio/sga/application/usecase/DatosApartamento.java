package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;

import java.util.List;
import java.util.Set;

/** Datos descriptivos de un apartamento (7.3), comunes a crearlo y a actualizarlo. */
public record DatosApartamento(String nombre, String descripcion, int dormitorios, Capacidad capacidad,
                               boolean admiteMascotas, String advertenciaAcceso, Set<Caracteristica> caracteristicas,
                               List<ImagenApartamento> imagenes) {
}

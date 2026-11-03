package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.ApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.response.ActualizacionApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.UbicacionDTO;
import co.edu.uniquindio.sga.application.usecase.ActualizarApartamentoUseCase.ApartamentoActualizado;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase.ApartamentoDisponible;
import co.edu.uniquindio.sga.application.usecase.DatosApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** DTO ↔ dominio del recurso Apartamento (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface ApartamentoWebMapper {

    @Mapping(target = "capacidad", expression = "java(DominioWebMapperSoporte.capacidadDesdeEntero(request.capacidad()))")
    DatosApartamento aDatos(ApartamentoRequest request);

    default ApartamentoResumenResponse aResumen(Apartamento apartamento, Long valorEstimado) {
        return new ApartamentoResumenResponse(apartamento.identificacion().valor(), apartamento.nombre(),
                apartamento.capacidad().personas(), apartamento.dormitorios(), imagenPrincipal(apartamento.imagenes()),
                apartamento.caracteristicas().stream().map(c -> c.nombre()).toList(), apartamento.admiteMascotas(),
                apartamento.advertenciaAcceso().orElse(null), valorEstimado);
    }

    default ApartamentoResumenResponse aResumen(ApartamentoDisponible disponible) {
        return aResumen(disponible.apartamento(), DominioWebMapperSoporte.dineroALong(disponible.valorEstimado().total()));
    }

    default ApartamentoDetalleResponse aDetalle(Apartamento apartamento, UbicacionDTO ubicacion) {
        return new ApartamentoDetalleResponse(apartamento.identificacion().valor(), apartamento.nombre(),
                apartamento.descripcion(), apartamento.dormitorios(), apartamento.capacidad().personas(),
                apartamento.caracteristicas().stream().map(c -> c.nombre()).toList(), apartamento.admiteMascotas(),
                apartamento.advertenciaAcceso().orElse(null),
                apartamento.imagenes().stream().map(DominioWebMapperSoporte::imagenADto).toList(),
                apartamento.estadoOperativo(), apartamento.estaActivo(), ubicacion);
    }

    default ActualizacionApartamentoResponse aActualizacion(ApartamentoActualizado resultado, UbicacionDTO ubicacion) {
        return new ActualizacionApartamentoResponse(aDetalle(resultado.apartamento(), ubicacion),
                codigosComoTexto(resultado.reservasSobreNuevaCapacidad()));
    }

    default String imagenPrincipal(List<ImagenApartamento> imagenes) {
        return imagenes.stream()
                .filter(ImagenApartamento::principal)
                .map(ImagenApartamento::url)
                .findFirst()
                .orElseGet(() -> imagenes.isEmpty() ? null : imagenes.get(0).url());
    }

    default List<String> codigosComoTexto(List<CodigoReserva> codigos) {
        return codigos.stream().map(CodigoReserva::valor).toList();
    }
}

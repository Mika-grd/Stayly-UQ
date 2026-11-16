package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.request.TarifaTemporadaDTO;
import co.edu.uniquindio.sga.application.dto.response.TarifaVigenteDTO;
import co.edu.uniquindio.sga.application.dto.response.TarifarioResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarTarifasUseCase.TarifaDefinida;
import co.edu.uniquindio.sga.application.usecase.GestionarTarifasUseCase.TarifarioConsultado;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifa;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** DTO ↔ dominio del recurso Tarifas (sin lógica de negocio: solo cambia de forma). */
@Mapper(componentModel = "spring", uses = DominioWebMapperSoporte.class)
public interface TarifaWebMapper {

    @Mapping(target = "temporada", source = "idTemporada")
    @Mapping(target = "valor", source = "valor")
    TarifaDefinida aTarifaDefinida(TarifaTemporadaDTO dto);

    List<TarifaDefinida> aTarifasDefinidas(List<TarifaTemporadaDTO> tarifas);

    default List<TarifaDefinida> aTarifasDefinidas(TarifaRequest request) {
        return aTarifasDefinidas(request.tarifas());
    }

    default TarifaVigenteDTO aVigenteDTO(Tarifa tarifa, String nombreTemporada) {
        return new TarifaVigenteDTO(tarifa.temporada().valor(), nombreTemporada,
                DominioWebMapperSoporte.dineroALong(tarifa.valor()), tarifa.vigenteDesde());
    }

    default TarifarioResponse aTarifarioResponse(TarifarioConsultado consultado) {
        Map<IdTemporada, String> nombres = consultado.temporadas().stream()
                .collect(Collectors.toMap(Temporada::id, Temporada::nombre));
        List<TarifaVigenteDTO> vigentes = consultado.temporadas().stream()
                .map(temporada -> consultado.tarifario().tarifaVigente(temporada.id())
                        .map(tarifa -> aVigenteDTO(tarifa, temporada.nombre())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<TarifaVigenteDTO> historico = consultado.tarifario().historico().stream()
                .map(tarifa -> aVigenteDTO(tarifa, nombres.getOrDefault(tarifa.temporada(), "")))
                .toList();
        return new TarifarioResponse(consultado.tarifario().apartamento().valor(), vigentes, historico,
                consultado.completo());
    }
}

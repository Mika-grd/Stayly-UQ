package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TarifaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifarioEntity;

/**
 * Traduce entre el agregado Tarifario y su tabla. Se rearma con su propia API pública
 * ({@code nuevo} + {@code definirTarifa}), en el mismo orden en que se definieron las tarifas.
 */
public final class TarifarioMapper {

    private TarifarioMapper() {
    }

    public static TarifarioEntity aEntidad(Tarifario tarifario) {
        return new TarifarioEntity(tarifario.apartamento().valor(),
                tarifario.historico().stream()
                        .map(tarifa -> new TarifaEmbeddable(tarifa.temporada().valor(), tarifa.valor().valor(),
                                tarifa.vigenteDesde()))
                        .toList());
    }

    public static Tarifario aDominio(TarifarioEntity entidad) {
        Tarifario tarifario = Tarifario.nuevo(new IdentificacionApartamento(entidad.getApartamento()));
        entidad.getTarifas().forEach(tarifa -> tarifario.definirTarifa(new IdTemporada(tarifa.getTemporada()),
                new Dinero(tarifa.getValor()), tarifa.getVigenteDesde()));
        return tarifario;
    }
}

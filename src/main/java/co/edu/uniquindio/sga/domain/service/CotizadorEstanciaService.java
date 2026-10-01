package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.alojamiento.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.CargoNoche;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifa;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * RN-05, RN-06 y 3.4: liquida la estancia noche por noche con la tarifa de la temporada de cada
 * noche × ocupantes facturables a la fecha de entrada. El redondeo se aplica al total.
 */
public class CotizadorEstanciaService {

    private final TemporadaRepository temporadas;
    private final TarifarioRepository tarifarios;
    private final ConfiguracionAlojamiento configuracion;

    public CotizadorEstanciaService(TemporadaRepository temporadas, TarifarioRepository tarifarios,
                                    ConfiguracionAlojamiento configuracion) {
        this.temporadas = temporadas;
        this.tarifarios = tarifarios;
        this.configuracion = configuracion;
    }

    public ValorCongelado cotizar(IdentificacionApartamento apartamento, Estancia estancia, ComposicionGrupo grupo) {
        Tarifario tarifario = tarifarios.buscarPorApartamento(apartamento)
                .orElseThrow(() -> tarifasIncompletas(apartamento));
        UmbralEdadFacturable umbral = configuracion.umbralEdad();
        int facturables = grupo.facturablesEn(estancia.entrada(), umbral);
        List<CargoNoche> noches = estancia.fechasDeNoches().stream()
                .map(noche -> liquidarNoche(tarifario, noche, facturables))
                .toList();
        return new ValorCongelado(noches);
    }

    private CargoNoche liquidarNoche(Tarifario tarifario, LocalDate noche, int facturables) {
        Temporada temporada = temporadas.buscarTemporadaDe(noche);
        Tarifa tarifa = tarifario.tarifaVigente(temporada.id())
                .orElseThrow(() -> tarifasIncompletas(tarifario.apartamento()));
        return new CargoNoche(noche, temporada.nombre(), tarifa.valor(), facturables);
    }

    private static ReglaDominioException tarifasIncompletas(IdentificacionApartamento apartamento) {
        return new ReglaDominioException("TARIFAS_INCOMPLETAS",
                "El apartamento " + apartamento + " no tiene tarifa en todas las temporadas.");
    }
}

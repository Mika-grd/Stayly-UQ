package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.tarifario.Tarifario;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;
import co.edu.uniquindio.sga.domain.model.temporada.Temporada;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 7.4: define las tarifas vigentes de un apartamento por temporada y consulta su tarifario. Una
 * tarifa nueva nunca borra la anterior: queda en el histórico (TA-2). Tarifa valida que el valor
 * sea positivo (TA-1).
 */
@Service
@Transactional
public class GestionarTarifasUseCase {

    private final ApartamentoRepository apartamentos;
    private final TemporadaRepository temporadas;
    private final TarifarioRepository tarifarios;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final Clock reloj;

    public GestionarTarifasUseCase(ApartamentoRepository apartamentos, TemporadaRepository temporadas,
                                   TarifarioRepository tarifarios,
                                   VerificadorTarifasCompletasService verificadorTarifas, Clock reloj) {
        this.apartamentos = apartamentos;
        this.temporadas = temporadas;
        this.tarifarios = tarifarios;
        this.verificadorTarifas = verificadorTarifas;
        this.reloj = reloj;
    }

    public TarifarioConsultado definir(IdentificacionApartamento apartamento, List<TarifaDefinida> tarifas) {
        Consultas.apartamento(apartamentos, apartamento);
        Tarifario tarifario = tarifarios.buscarPorApartamento(apartamento).orElseGet(() -> Tarifario.nuevo(apartamento));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (TarifaDefinida tarifa : tarifas) {
            Consultas.temporada(temporadas, tarifa.temporada());
            tarifario.definirTarifa(tarifa.temporada(), tarifa.valor(), ahora);
        }
        tarifarios.guardar(tarifario);
        return consultado(tarifario);
    }

    @Transactional(readOnly = true)
    public TarifarioConsultado consultar(IdentificacionApartamento apartamento) {
        Consultas.apartamento(apartamentos, apartamento);
        return consultado(tarifarios.buscarPorApartamento(apartamento).orElseGet(() -> Tarifario.nuevo(apartamento)));
    }

    private TarifarioConsultado consultado(Tarifario tarifario) {
        return new TarifarioConsultado(tarifario, temporadas.listarTodas(),
                verificadorTarifas.tieneTarifasCompletas(tarifario.apartamento()));
    }

    public record TarifaDefinida(IdTemporada temporada, Dinero valor) {
    }

    /** El tarifario con las temporadas (para mostrar sus nombres) y si tiene tarifa en todas. */
    public record TarifarioConsultado(Tarifario tarifario, List<Temporada> temporadas, boolean completo) {
    }
}

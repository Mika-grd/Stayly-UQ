package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 7.1: calendario de disponibilidad del detalle de un apartamento, para el huésped. Cada noche del
 * rango [desde, hasta) se evalúa como una estancia de una noche con DisponibilidadApartamentoService
 * (RN-01, RN-07, RN-12, RN-20), así que el calendario aplica las mismas reglas que la búsqueda. No
 * expone reservas ni bloqueos: solo si la noche se puede vender.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarDisponibilidadApartamentoUseCase {

    /** Tope del rango para que una sola consulta no recorra el año entero. */
    static final int MAXIMO_NOCHES = 93;
    private static final int UN_OCUPANTE = 1;

    private final ApartamentoRepository apartamentos;
    private final DisponibilidadApartamentoService disponibilidad;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final Clock reloj;

    public ConsultarDisponibilidadApartamentoUseCase(ApartamentoRepository apartamentos,
                                                     DisponibilidadApartamentoService disponibilidad,
                                                     VerificadorTarifasCompletasService verificadorTarifas,
                                                     Clock reloj) {
        this.apartamentos = apartamentos;
        this.disponibilidad = disponibilidad;
        this.verificadorTarifas = verificadorTarifas;
        this.reloj = reloj;
    }

    public CalendarioDisponibilidad ejecutar(IdentificacionApartamento identificacion, RangoFechas rango) {
        if (ChronoUnit.DAYS.between(rango.inicio(), rango.fin()) > MAXIMO_NOCHES) {
            throw new ReglaDominioException("RANGO_FECHAS_INVALIDO",
                    "El calendario de disponibilidad abarca como máximo " + MAXIMO_NOCHES + " noches.");
        }
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        boolean vendible = verificadorTarifas.tieneTarifasCompletas(identificacion);
        LocalDate hoy = LocalDate.now(reloj);
        List<NocheDisponible> noches = new Estancia(rango.inicio(), rango.fin()).fechasDeNoches().stream()
                .map(noche -> new NocheDisponible(noche, vendible && !noche.isBefore(hoy)
                        && disponibilidad.estaDisponible(apartamento, new Estancia(noche, noche.plusDays(1)),
                        UN_OCUPANTE)))
                .toList();
        return new CalendarioDisponibilidad(identificacion, rango, noches);
    }

    public record NocheDisponible(LocalDate fecha, boolean disponible) {
    }

    public record CalendarioDisponibilidad(IdentificacionApartamento apartamento, RangoFechas rango,
                                           List<NocheDisponible> noches) {
    }
}

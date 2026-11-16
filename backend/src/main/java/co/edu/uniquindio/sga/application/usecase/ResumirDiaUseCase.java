package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.AsistenteIA;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resumen del día para recepción redactado por el modelo de IA (10.2): qué llega, qué sale, qué
 * falta preparar y a qué prestar atención. Al modelo solo van códigos, apartamentos, horas y
 * cantidades; nunca nombres, documentos ni contactos de los huéspedes. Si el modelo no responde, la
 * consulta igual termina: el resumen queda "no disponible" y recepción opera con sus listas (degradación
 * visible, nunca bloqueo).
 */
@Service
@Transactional(readOnly = true)
public class ResumirDiaUseCase {

    static final String INSTRUCCION = """
            Eres el asistente de recepción de un alojamiento de apartamentos en Colombia. Con los datos del día,
            escribe en español un resumen operativo de 3 a 5 frases cortas para el equipo: cuántos grupos llegan y
            salen, en qué orden conviene preparar los apartamentos, y qué requiere atención (llegadas sin hora
            estimada, llegadas nocturnas, mascotas, apartamentos que aún no están listos para una llegada).
            No inventes datos que no estén en la lista. Sin saludos ni viñetas.""";

    private final ConsultarMovimientosDelDiaUseCase movimientos;
    private final ConsultarApartamentosPorPrepararUseCase porPreparar;
    private final AsistenteIA asistente;

    public ResumirDiaUseCase(ConsultarMovimientosDelDiaUseCase movimientos,
                             ConsultarApartamentosPorPrepararUseCase porPreparar, AsistenteIA asistente) {
        this.movimientos = movimientos;
        this.porPreparar = porPreparar;
        this.asistente = asistente;
    }

    public ResumenDia ejecutar(LocalDate fecha) {
        var dia = movimientos.ejecutar(fecha);
        String datos = datosDelDia(dia.fecha(), dia.llegadas(), dia.salidas(), porPreparar.ejecutar());
        Optional<String> texto;
        try {
            texto = asistente.redactar(INSTRUCCION, datos).map(String::trim).filter(t -> !t.isEmpty());
        } catch (RuntimeException e) {
            texto = Optional.empty();
        }
        return new ResumenDia(dia.fecha(), texto, asistente.proveedor());
    }

    /** Hechos del día en texto plano, sin datos personales de los huéspedes. */
    static String datosDelDia(LocalDate fecha, List<Reserva> llegadas, List<Reserva> salidas,
                              List<Apartamento> pendientes) {
        StringBuilder datos = new StringBuilder("Fecha: ").append(fecha).append('\n');
        datos.append("Llegadas (").append(llegadas.size()).append("):\n");
        llegadas.stream().sorted(Comparator.comparing(r -> r.apartamento().valor())).forEach(r -> datos
                .append("- ").append(r.codigo().valor()).append(", apartamento ").append(r.apartamento().valor())
                .append(", ").append(r.ocupantes().size()).append(" personas")
                .append(", hora estimada ")
                .append(r.horaEstimadaLlegada().map(h -> h.hora().toString()).orElse("sin indicar"))
                .append(r.mascotas().cantidad() > 0 ? ", " + r.mascotas().cantidad() + " mascota(s)" : "")
                .append(", estado ").append(r.estado()).append('\n'));
        datos.append("Salidas (").append(salidas.size()).append("):\n");
        salidas.stream().sorted(Comparator.comparing(r -> r.apartamento().valor())).forEach(r -> datos
                .append("- ").append(r.codigo().valor()).append(", apartamento ").append(r.apartamento().valor())
                .append(", estado ").append(r.estado()).append('\n'));
        datos.append("Apartamentos sin preparar: ").append(pendientes.isEmpty() ? "ninguno" : pendientes.stream()
                .map(a -> a.identificacion().valor() + " (" + a.estadoOperativo() + ")")
                .collect(Collectors.joining(", "))).append('\n');
        return datos.toString();
    }

    public record ResumenDia(LocalDate fecha, Optional<String> texto, String proveedor) {

        public boolean disponible() {
            return texto.isPresent();
        }
    }
}

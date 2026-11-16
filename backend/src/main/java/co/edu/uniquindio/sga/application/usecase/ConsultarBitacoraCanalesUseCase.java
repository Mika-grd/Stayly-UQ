package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 7.8: los eventos de integración de todos los canales, del más reciente al más antiguo, filtrados por
 * nombre de canal y por fechas (ambas inclusive). Son pocos canales, así que se filtra en memoria.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarBitacoraCanalesUseCase {

    private final CanalRepository canales;

    public ConsultarBitacoraCanalesUseCase(CanalRepository canales) {
        this.canales = canales;
    }

    public Pagina<Entrada> ejecutar(String nombreCanal, LocalDate desde, LocalDate hasta, PeticionPagina pagina) {
        List<Entrada> entradas = canales.listar().stream()
                .filter(canal -> nombreCanal == null || nombreCanal.isBlank()
                        || canal.nombre().equalsIgnoreCase(nombreCanal.trim()))
                .flatMap(canal -> IntStream.range(0, canal.eventos().size())
                        .mapToObj(indice -> new Entrada(canal, indice, canal.eventos().get(indice))))
                .filter(entrada -> desde == null || !entrada.evento().fecha().toLocalDate().isBefore(desde))
                .filter(entrada -> hasta == null || !entrada.evento().fecha().toLocalDate().isAfter(hasta))
                .sorted(Comparator.comparing((Entrada entrada) -> entrada.evento().fecha()).reversed())
                .toList();
        return Pagina.de(entradas, pagina);
    }

    /**
     * Un evento con su canal. La bitácora solo crece (C-1), así que la posición del evento en ella lo
     * identifica para siempre: el id se deriva de canal + posición.
     */
    public record Entrada(Canal canal, int indice, EventoCanal evento) {

        public UUID id() {
            return UUID.nameUUIDFromBytes((canal.id().valor() + "#" + indice).getBytes(StandardCharsets.UTF_8));
        }
    }
}

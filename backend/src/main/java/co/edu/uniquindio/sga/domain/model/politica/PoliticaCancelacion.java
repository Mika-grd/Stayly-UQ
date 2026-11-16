package co.edu.uniquindio.sga.domain.model.politica;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado PoliticaCancelacion (F-07). Cada versión es inmutable (P-2): un cambio
 * crea una versión nueva con {@link #nuevaVersion}. Invariante P-1.
 */
public class PoliticaCancelacion {

    private final VersionPolitica version;
    private final List<TramoPolitica> tramos;
    private final int porcentajeRetencionNoShow;
    private final LocalDateTime vigenteDesde;

    private PoliticaCancelacion(VersionPolitica version, List<TramoPolitica> tramos, int porcentajeRetencionNoShow,
                                LocalDateTime vigenteDesde) {
        this.version = version;
        this.tramos = tramos.stream()
                .sorted(Comparator.comparingInt(TramoPolitica::antelacionMinimaDias).reversed())
                .toList();
        this.porcentajeRetencionNoShow = porcentajeRetencionNoShow;
        this.vigenteDesde = vigenteDesde;
    }

    /** P-1: al menos dos tramos, sin antelaciones repetidas, retenciones entre 0 % y 100 %. */
    public static PoliticaCancelacion crearVersion(VersionPolitica version, List<TramoPolitica> tramos,
                                                   int porcentajeRetencionNoShow, LocalDateTime vigenteDesde) {
        if (version == null || vigenteDesde == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La política requiere versión y fecha de vigencia.");
        }
        if (tramos == null || tramos.size() < 2 || tramos.stream().anyMatch(Objects::isNull)
                || tramos.stream().map(TramoPolitica::antelacionMinimaDias).distinct().count() != tramos.size()) {
            throw new ReglaDominioException("TRAMOS_INSUFICIENTES",
                    "La política debe tener al menos dos tramos de antelación distintos.");
        }
        if (porcentajeRetencionNoShow < 0 || porcentajeRetencionNoShow > 100) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO",
                    "La retención por no-show debe estar entre 0 % y 100 %.");
        }
        return new PoliticaCancelacion(version, tramos, porcentajeRetencionNoShow, vigenteDesde);
    }

    public PoliticaCancelacion nuevaVersion(List<TramoPolitica> tramos, int porcentajeRetencionNoShow,
                                            LocalDateTime vigenteDesde) {
        return crearVersion(new VersionPolitica(version.numero() + 1), tramos, porcentajeRetencionNoShow,
                vigenteDesde);
    }

    /**
     * Aplica el tramo de mayor antelación mínima que se cumpla. Si ninguno se cumple (la
     * política no definió un tramo desde 0 días) se aplica el más estricto.
     */
    public Dinero retencionPara(Dinero valor, long diasAntelacion) {
        TramoPolitica tramo = tramos.stream()
                .filter(t -> diasAntelacion >= t.antelacionMinimaDias())
                .findFirst()
                .orElse(tramos.get(tramos.size() - 1));
        return valor.porcentaje(tramo.porcentajeRetencion()).redondear();
    }

    public Dinero retencionNoShow(Dinero valor) {
        return valor.porcentaje(porcentajeRetencionNoShow).redondear();
    }

    public VersionPolitica version() { return version; }
    public List<TramoPolitica> tramos() { return tramos; }
    public int porcentajeRetencionNoShow() { return porcentajeRetencionNoShow; }
    public LocalDateTime vigenteDesde() { return vigenteDesde; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PoliticaCancelacion otra && version.equals(otra.version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}

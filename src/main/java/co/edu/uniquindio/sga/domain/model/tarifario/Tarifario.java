package co.edu.uniquindio.sga.domain.model.tarifario;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Tarifario: uno por apartamento (su identidad es la IdentificacionApartamento).
 * TA-2: una tarifa nueva nunca borra la anterior, queda en el histórico.
 */
public class Tarifario {

    private final IdentificacionApartamento apartamento;
    private final List<Tarifa> tarifas = new ArrayList<>();

    private Tarifario(IdentificacionApartamento apartamento) {
        this.apartamento = apartamento;
    }

    public static Tarifario nuevo(IdentificacionApartamento apartamento) {
        if (apartamento == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "El tarifario requiere la identificación del apartamento.");
        }
        return new Tarifario(apartamento);
    }

    public void definirTarifa(IdTemporada temporada, Dinero valor, LocalDateTime desde) {
        tarifas.add(new Tarifa(temporada, valor, desde));
    }

    /** La última definida para esa temporada; las anteriores quedan en el histórico. */
    public Optional<Tarifa> tarifaVigente(IdTemporada temporada) {
        return tarifas.stream()
                .filter(tarifa -> tarifa.temporada().equals(temporada))
                .max(Comparator.comparing(Tarifa::vigenteDesde));
    }

    public boolean tieneTarifaPara(IdTemporada temporada) {
        return tarifaVigente(temporada).isPresent();
    }

    public List<Tarifa> historico() {
        return Collections.unmodifiableList(tarifas);
    }

    public IdentificacionApartamento apartamento() { return apartamento; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Tarifario otro && apartamento.equals(otro.apartamento));
    }

    @Override
    public int hashCode() {
        return Objects.hash(apartamento);
    }
}

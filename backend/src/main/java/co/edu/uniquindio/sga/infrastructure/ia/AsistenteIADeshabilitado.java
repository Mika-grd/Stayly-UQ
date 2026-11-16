package co.edu.uniquindio.sga.infrastructure.ia;

import co.edu.uniquindio.sga.domain.repository.AsistenteIA;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Alternativa local del modelo (I-02): no redacta nada. Es la opción por defecto y la de las pruebas
 * (I-08); demuestra que el sistema opera con la IA apagada cambiando solo {@code sga.ia.proveedor}.
 */
@Component
@ConditionalOnProperty(name = "sga.ia.proveedor", havingValue = "ninguno", matchIfMissing = true)
public class AsistenteIADeshabilitado implements AsistenteIA {

    @Override
    public Optional<String> redactar(String instruccion, String datos) {
        return Optional.empty();
    }

    @Override
    public String proveedor() {
        return "ninguno";
    }
}

package co.edu.uniquindio.sga.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rúbrica de la Entrega 2: el openapi.yaml es el contrato. Cada endpoint implementado está en el
 * contrato y cada operación del contrato que no está marcada "(pendiente del corte 3)" está implementada.
 * También comprueba que el archivo sea YAML válido.
 */
@SpringBootTest
class ContratoOpenApiTest {

    private static final Path CONTRATO = Path.of("..", "docs", "api", "openapi.yaml");
    private static final Set<String> METODOS = Set.of("get", "post", "put", "delete", "patch");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapeos;

    @Test
    @DisplayName("Lo implementado y lo documentado coinciden, operación por operación")
    void contratoCoherenteConLosControllers() throws IOException {
        Set<String> documentadas = new TreeSet<>();
        Set<String> pendientes = new TreeSet<>();
        leerContrato(documentadas, pendientes);

        Set<String> implementadas = new TreeSet<>();
        for (RequestMappingInfo info : mapeos.getHandlerMethods().keySet()) {
            for (String ruta : info.getPatternValues()) {
                if (ruta.startsWith("/api/")) {
                    info.getMethodsCondition().getMethods()
                            .forEach(metodo -> implementadas.add(metodo.name() + " " + ruta));
                }
            }
        }

        assertThat(implementadas).as("endpoints implementados que no están en openapi.yaml")
                .isSubsetOf(documentadas);
        Set<String> sinImplementar = new TreeSet<>(documentadas);
        sinImplementar.removeAll(implementadas);
        assertThat(sinImplementar).as("operaciones del contrato sin implementar y sin marcar como pendientes")
                .isEqualTo(pendientes);
    }

    @SuppressWarnings("unchecked")
    private static void leerContrato(Set<String> documentadas, Set<String> pendientes) throws IOException {
        try (Reader lector = Files.newBufferedReader(CONTRATO, StandardCharsets.UTF_8)) {
            Map<String, Object> raiz = new Yaml().load(lector);
            Map<String, Map<String, Object>> rutas = (Map<String, Map<String, Object>>) raiz.get("paths");
            rutas.forEach((ruta, operaciones) -> operaciones.forEach((metodo, operacion) -> {
                if (METODOS.contains(metodo)) {
                    String clave = metodo.toUpperCase() + " " + ruta;
                    documentadas.add(clave);
                    Object resumen = ((Map<String, Object>) operacion).get("summary");
                    if (resumen != null && resumen.toString().endsWith("(pendiente del corte 3)")) {
                        pendientes.add(clave);
                    }
                }
            }));
        }
    }
}

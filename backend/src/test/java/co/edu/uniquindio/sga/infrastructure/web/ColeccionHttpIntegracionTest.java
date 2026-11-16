package co.edu.uniquindio.sga.infrastructure.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ejecuta docs/api/coleccion-sga.http tal como está, en orden, contra la aplicación completa recién
 * arrancada (base H2 propia, con los datos de prueba de 12.3). Lee de cada petición sus líneas
 * {@code # espera:} (HTTP y código de negocio) y {@code # guarda:} (variable que toma de la respuesta),
 * así la colección que se usa en la sustentación queda verificada con {@code ./gradlew test}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:coleccion;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ColeccionHttpIntegracionTest {

    private static final Path COLECCION = Path.of("..", "docs", "api", "coleccion-sga.http");
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{(\\w+)}}");
    private static final Pattern HOY_MAS = Pattern.compile("hoyMas(\\d+)");
    private static final Pattern ESPERA = Pattern.compile("^# espera: (\\d{3})(?: (\\w+))?$");
    private static final Pattern GUARDA = Pattern.compile("^# guarda: (\\w+) = (\\S+)$");

    @Autowired
    private MockMvc mvc;

    private final Map<String, String> variables = new HashMap<>();

    @TestFactory
    Stream<DynamicTest> coleccionCompleta() throws IOException {
        List<Peticion> peticiones = leer(Files.readAllLines(COLECCION, StandardCharsets.UTF_8));
        assertThat(peticiones).hasSizeGreaterThan(60);
        return peticiones.stream().map(peticion -> DynamicTest.dynamicTest(peticion.nombre(), () -> ejecutar(peticion)));
    }

    private void ejecutar(Peticion peticion) throws Exception {
        String url = sustituir(peticion.url()).replace(variables.get("base"), "");
        MockHttpServletRequestBuilder solicitud = MockMvcRequestBuilders.request(HttpMethod.valueOf(peticion.metodo()), url);
        peticion.encabezados().forEach((nombre, valor) -> solicitud.header(nombre, sustituir(valor)));
        if (!peticion.cuerpo().isBlank()) {
            solicitud.characterEncoding(StandardCharsets.UTF_8).content(sustituir(peticion.cuerpo()));
        }
        MockHttpServletResponse respuesta = mvc.perform(solicitud).andReturn().getResponse();
        String cuerpo = respuesta.getContentAsString(StandardCharsets.UTF_8);

        assertThat(respuesta.getStatus()).as("HTTP de «%s». Respuesta: %s", peticion.nombre(), cuerpo)
                .isEqualTo(peticion.estado());
        if (peticion.codigo() != null) {
            assertThat(JsonPath.<String>read(cuerpo, "$.codigo")).as("código de «%s»", peticion.nombre())
                    .isEqualTo(peticion.codigo());
        }
        peticion.guarda().forEach((variable, ruta) -> {
            Object valor = JsonPath.read(cuerpo, ruta);
            variables.put(variable, valor.toString());
        });
    }

    /** {{variable}}: las guardadas por peticiones anteriores, @base, hoy, ayer y hoyMasN (zona de Bogotá). */
    private String sustituir(String texto) {
        Matcher m = VARIABLE.matcher(texto);
        StringBuilder resultado = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(resultado, Matcher.quoteReplacement(valor(m.group(1))));
        }
        m.appendTail(resultado);
        return resultado.toString();
    }

    private String valor(String nombre) {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        Matcher hoyMas = HOY_MAS.matcher(nombre);
        if (hoyMas.matches()) {
            return hoy.plusDays(Long.parseLong(hoyMas.group(1))).toString();
        }
        return switch (nombre) {
            case "hoy" -> hoy.toString();
            case "ayer" -> hoy.minusDays(1).toString();
            default -> {
                assertThat(variables).as("variable {{%s}}", nombre).containsKey(nombre);
                yield variables.get(nombre);
            }
        };
    }

    /** Lector mínimo del formato .http: variables @x = y, bloques ###, comentarios, encabezados y cuerpo. */
    private List<Peticion> leer(List<String> lineas) {
        List<Peticion> peticiones = new ArrayList<>();
        Constructor actual = null;
        for (String linea : lineas) {
            if (linea.startsWith("###")) {
                if (actual != null) {
                    peticiones.add(actual.construir());
                }
                actual = new Constructor(linea.substring(3).trim());
            } else if (actual == null) {
                if (linea.startsWith("@") && linea.contains("=")) {
                    variables.put(linea.substring(1, linea.indexOf('=')).trim(),
                            linea.substring(linea.indexOf('=') + 1).trim());
                }
            } else {
                actual.agregar(linea);
            }
        }
        if (actual != null) {
            peticiones.add(actual.construir());
        }
        return peticiones;
    }

    private record Peticion(String nombre, String metodo, String url, Map<String, String> encabezados, String cuerpo,
                            int estado, String codigo, Map<String, String> guarda) {
    }

    private static final class Constructor {
        private final String nombre;
        private String metodo;
        private String url;
        private final Map<String, String> encabezados = new LinkedHashMap<>();
        private final StringBuilder cuerpo = new StringBuilder();
        private int estado;
        private String codigo;
        private final Map<String, String> guarda = new LinkedHashMap<>();
        private boolean enCuerpo;
        private boolean enScript;
        private boolean terminado;

        Constructor(String nombre) {
            this.nombre = nombre;
        }

        void agregar(String linea) {
            if (terminado) {
                return;
            }
            if (enScript) {
                terminado = linea.startsWith("%}");
                enScript = !terminado;
                return;
            }
            if (linea.startsWith("> {%")) {
                enScript = true;
                return;
            }
            if (metodo == null) {
                Matcher espera = ESPERA.matcher(linea);
                Matcher guardaLinea = GUARDA.matcher(linea);
                if (espera.matches()) {
                    estado = Integer.parseInt(espera.group(1));
                    codigo = espera.group(2);
                } else if (guardaLinea.matches()) {
                    guarda.put(guardaLinea.group(1), guardaLinea.group(2));
                } else if (!linea.isBlank() && !linea.startsWith("#") && !linea.startsWith("//")) {
                    String[] partes = linea.trim().split("\\s+");
                    metodo = partes[0];
                    url = partes[1];
                }
            } else if (!enCuerpo) {
                if (linea.isBlank()) {
                    enCuerpo = true;
                } else {
                    encabezados.put(linea.substring(0, linea.indexOf(':')).trim(),
                            linea.substring(linea.indexOf(':') + 1).trim());
                }
            } else {
                cuerpo.append(linea).append('\n');
            }
        }

        Peticion construir() {
            assertThat(estado).as("«%s» sin línea # espera:", nombre).isPositive();
            return new Peticion(nombre, metodo, url, encabezados, cuerpo.toString().trim(), estado, codigo, guarda);
        }
    }
}

package co.edu.uniquindio.sga.infrastructure.ia;

import co.edu.uniquindio.sga.domain.repository.AsistenteIA;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Modelo de IA por el API de chat compatible con OpenAI ({@code POST {url}/chat/completions}). Sirve
 * con proveedores de nivel gratuito (Groq, Google Gemini) o con un modelo local (Ollama), sin
 * cambiar código: la URL, el modelo y la clave son configuración (I-06, I-07). Tiempo máximo
 * explícito (I-03); ante error, respuesta inesperada o demora devuelve vacío y deja constancia en el
 * registro (I-04), así quien lo usa sigue sin IA.
 */
@Component
@ConditionalOnProperty(name = "sga.ia.proveedor", havingValue = "openai-compatible")
public class AsistenteIACompatibleOpenAI implements AsistenteIA {

    private static final Logger LOG = LoggerFactory.getLogger(AsistenteIACompatibleOpenAI.class);

    private final HttpClient cliente;
    private final ObjectMapper json;
    private final URI chat;
    private final String modelo;
    private final String clave;
    private final Duration tiempoMaximo;

    public AsistenteIACompatibleOpenAI(ObjectMapper json, @Value("${sga.ia.url}") String url,
                                       @Value("${sga.ia.modelo}") String modelo,
                                       @Value("${sga.ia.clave:}") String clave,
                                       @Value("${sga.ia.tiempo-maximo:PT8S}") Duration tiempoMaximo) {
        this.json = json;
        this.chat = URI.create(url.replaceAll("/+$", "") + "/chat/completions");
        this.modelo = modelo;
        this.clave = clave;
        this.tiempoMaximo = tiempoMaximo;
        // HTTP/1.1: lo hablan todos los proveedores y los servidores locales (Ollama) sin negociar HTTP/2
        this.cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(tiempoMaximo).build();
    }

    @Override
    public Optional<String> redactar(String instruccion, String datos) {
        try {
            String cuerpo = json.writeValueAsString(Map.of(
                    "model", modelo,
                    "temperature", 0.3,
                    "messages", List.of(
                            Map.of("role", "system", "content", instruccion),
                            Map.of("role", "user", "content", datos))));
            HttpRequest.Builder peticion = HttpRequest.newBuilder(chat)
                    .timeout(tiempoMaximo)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
            if (!clave.isBlank()) {
                peticion.header("Authorization", "Bearer " + clave);
            }
            HttpResponse<String> respuesta = cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() / 100 != 2) {
                LOG.warn("El modelo de IA respondió {}; se sigue sin IA", respuesta.statusCode());
                return Optional.empty();
            }
            JsonNode contenido = json.readTree(respuesta.body())
                    .path("choices").path(0).path("message").path("content");
            return contenido.isTextual() ? Optional.of(contenido.asText()) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("El modelo de IA no respondió a tiempo o falló ({}); se sigue sin IA",
                    e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public String proveedor() {
        return modelo;
    }
}

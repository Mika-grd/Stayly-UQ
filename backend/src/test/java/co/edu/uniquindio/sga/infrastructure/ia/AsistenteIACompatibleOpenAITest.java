package co.edu.uniquindio.sga.infrastructure.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** Adaptador HTTP contra un servidor local falso: las pruebas nunca llaman a un proveedor real (I-08). */
class AsistenteIACompatibleOpenAITest {

    private HttpServer servidor;
    private final AtomicReference<String> autorizacion = new AtomicReference<>();
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();

    @AfterEach
    void apagar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    private String levantar(int estado, String respuesta, long demoraMs) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/v1/chat/completions", intercambio -> {
            autorizacion.set(intercambio.getRequestHeaders().getFirst("Authorization"));
            cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            try {
                Thread.sleep(demoraMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(estado, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1/";
    }

    private AsistenteIACompatibleOpenAI asistente(String url, String clave, Duration maximo) {
        return new AsistenteIACompatibleOpenAI(new ObjectMapper(), url, "llama3.2", clave, maximo);
    }

    @Test
    @DisplayName("10.2 · Envía instrucción y datos al chat compatible con OpenAI y devuelve el texto")
    void respuestaCorrecta() throws IOException {
        // Arrange
        String url = levantar(200,
                "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Dos llegadas.\"}}]}", 0);

        // Act
        var texto = asistente(url, "clave-de-prueba", Duration.ofSeconds(2)).redactar("Resuma", "Llegadas (2)");

        // Assert
        assertThat(texto).contains("Dos llegadas.");
        assertThat(autorizacion.get()).isEqualTo("Bearer clave-de-prueba");
        assertThat(cuerpoRecibido.get()).contains("\"model\":\"llama3.2\"", "Llegadas (2)");
    }

    @Test
    @DisplayName("I-03/I-04 · Error o demora mayor al tiempo máximo: vacío, sin excepción (caso de falla)")
    void falloOTiempoAgotado() throws IOException {
        // Error 500
        String conError = levantar(500, "{\"error\":\"caído\"}", 0);
        assertThat(asistente(conError, "", Duration.ofSeconds(2)).redactar("Resuma", "x")).isEmpty();
        assertThat(autorizacion.get()).isNull();
        servidor.stop(0);

        // Respuesta lenta: se corta en el tiempo máximo
        String lento = levantar(200, "{\"choices\":[{\"message\":{\"content\":\"tarde\"}}]}", 3000);
        long inicio = System.nanoTime();
        assertThat(asistente(lento, "", Duration.ofMillis(300)).redactar("Resuma", "x")).isEmpty();
        assertThat(Duration.ofNanos(System.nanoTime() - inicio)).isLessThan(Duration.ofSeconds(2));

        // Sin servidor
        assertThat(asistente("http://127.0.0.1:9/v1", "", Duration.ofMillis(300)).redactar("Resuma", "x")).isEmpty();
    }
}

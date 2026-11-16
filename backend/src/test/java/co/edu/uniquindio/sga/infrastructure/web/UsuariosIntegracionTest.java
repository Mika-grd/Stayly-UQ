package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.security.NotificadorCorreo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 7.2: gestión de usuarios por el administrador, perfil propio, cambio y recuperación de contraseña. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:usuarios;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UsuariosIntegracionTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private BuzonDePrueba buzon;

    @BeforeEach
    void vaciarBuzon() {
        buzon.correos.clear();
    }

    @Test
    @DisplayName("CU-38 · El administrador crea un recepcionista que inicia sesión con su rol (201 con Location)")
    void crearUsuarioInterno() throws Exception {
        mvc.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("admin@stayly.local", "Admin2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("nueva.recepcion@stayly.local", "1000000050", "RECEPCIONISTA", "Turno2026")))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.rol").value("RECEPCIONISTA"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("nueva.recepcion@stayly.local", "Turno2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("RECEPCIONISTA"));
    }

    @Test
    @DisplayName("CU-38 · Solo el administrador gestiona usuarios (403) y el listado se pagina de a 10")
    void gestionSoloAdministrador() throws Exception {
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION,
                        bearer("recepcion@stayly.local", "Recepcion2026")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("admin@stayly.local", "Admin2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamano").value(10))
                .andExpect(jsonPath("$.contenido[0].correo").isNotEmpty());
    }

    @Test
    @DisplayName("CU-38 · El correo no se edita (409) y un titular con reservas activas no se elimina (409)")
    void reglasDeActualizarYEliminar() throws Exception {
        String admin = bearer("admin@stayly.local", "Admin2026");
        String idHuesped1 = idDe(admin, "huesped1@stayly.local");
        mvc.perform(put("/api/usuarios/{id}", idHuesped1).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("otro.correo@stayly.local", "1094000001", "RECEPCIONISTA", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_NO_EDITABLE"));
        mvc.perform(delete("/api/usuarios/{id}", idHuesped1).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("USUARIO_CON_RESERVAS_ACTIVAS"));
    }

    @Test
    @DisplayName("CU-38 · La eliminación es lógica: el usuario queda inactivo y ya no inicia sesión")
    void eliminacionLogica() throws Exception {
        String admin = bearer("admin@stayly.local", "Admin2026");
        String creado = mvc.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("temporal@stayly.local", "1000000060", "PERSONAL_SERVICIO", "Temporal2026")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = json.readTree(creado).get("id").asText();

        mvc.perform(delete("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("temporal@stayly.local", "Temporal2026")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CU-39 · El usuario cambia su perfil y su contraseña; con la actual errada responde 409")
    void perfilYContrasena() throws Exception {
        String token = bearer("huesped2@stayly.local", "Huesped2026");
        mvc.perform(put("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Carlos A. Mejía\",\"telefono\":\"3109998877\","
                                + "\"fotoUrl\":\"https://stayly-uq.co/perfil/carlos.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos A. Mejía"))
                .andExpect(jsonPath("$.correo").value("huesped2@stayly.local"))
                .andExpect(jsonPath("$.fotoUrl").value("https://stayly-uq.co/perfil/carlos.jpg"));
        mvc.perform(put("/api/usuarios/me/contrasena").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"Errada2026\",\"contrasenaNueva\":\"Nueva2026X\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONTRASENA_ACTUAL_INCORRECTA"));
        mvc.perform(put("/api/usuarios/me/contrasena").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"Huesped2026\",\"contrasenaNueva\":\"Nueva2026X\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("huesped2@stayly.local", "Nueva2026X")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CU-40 · Recuperación: el código llega al correo, sirve una sola vez y uno errado responde 409")
    void recuperarContrasena() throws Exception {
        mvc.perform(post("/api/auth/recuperacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"servicio@stayly.local\"}"))
                .andExpect(status().isAccepted());
        assertThat(buzon.correos).hasSize(1);
        Matcher codigo = Pattern.compile("\\b(\\d{6})\\b").matcher(buzon.correos.getFirst());
        assertThat(codigo.find()).isTrue();

        mvc.perform(post("/api/auth/recuperacion/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content(confirmacion("000000".equals(codigo.group(1)) ? "111111" : "000000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CODIGO_RECUPERACION_INVALIDO"));
        mvc.perform(post("/api/auth/recuperacion/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content(confirmacion(codigo.group(1))))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/recuperacion/confirmar").contentType(MediaType.APPLICATION_JSON)
                        .content(confirmacion(codigo.group(1))))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("servicio@stayly.local", "Recuperada2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CU-40 · Un correo no registrado también responde 202 y no se envía nada")
    void recuperacionNoRevelaCorreos() throws Exception {
        mvc.perform(post("/api/auth/recuperacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"nadie@stayly.local\"}"))
                .andExpect(status().isAccepted());
        assertThat(buzon.correos).isEmpty();
    }

    private static String confirmacion(String codigo) {
        return "{\"correo\":\"servicio@stayly.local\",\"codigo\":\"%s\",\"contrasenaNueva\":\"Recuperada2026\"}"
                .formatted(codigo);
    }

    private static String usuario(String correo, String documento, String rol, String contrasena) {
        return """
                {"nombre":"Usuario de prueba","correo":"%s","telefono":"3001112233","documentoIdentidad":"%s",
                 "fechaNacimiento":"1992-04-10","rol":"%s"%s}
                """.formatted(correo, documento, rol, contrasena == null ? "" : ",\"contrasena\":\"" + contrasena + "\"");
    }

    private String idDe(String admin, String correo) throws Exception {
        String pagina = mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString();
        for (var nodo : json.readTree(pagina).get("contenido")) {
            if (nodo.get("correo").asText().equals(correo)) {
                return nodo.get("id").asText();
            }
        }
        throw new AssertionError("No se encontró " + correo);
    }

    private static String credenciales(String correo, String contrasena) {
        return "{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena);
    }

    private String bearer(String correo, String contrasena) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(correo, contrasena)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(cuerpo).get("token").asText();
    }

    /** Buzón que guarda los correos en memoria para leer el código de recuperación. */
    static class BuzonDePrueba implements NotificadorCorreo {
        final List<String> correos = new ArrayList<>();

        @Override
        public void enviar(String destinatario, String asunto, String cuerpo) {
            correos.add(cuerpo);
        }
    }

    @TestConfiguration
    static class ConfiguracionBuzon {
        @Bean
        @Primary
        BuzonDePrueba buzonDePrueba() {
            return new BuzonDePrueba();
        }
    }
}

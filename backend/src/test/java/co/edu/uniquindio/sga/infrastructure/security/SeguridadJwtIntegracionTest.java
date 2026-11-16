package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Casos de la sección 9 de la Guía 12 contra la aplicación completa: login real contra H2, token
 * Bearer y matriz de roles y propietario de ReservaController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadJwtIntegracionTest {

    private static final String RECEPCION = "recepcion@stayly.local";
    private static final String HUESPED_1 = "huesped1@stayly.local";   // titular 1094000001
    private static final String HUESPED_2 = "huesped2@stayly.local";   // titular 1094000002
    private static final String SERVICIO = "servicio@stayly.local";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UsuarioJpaDataRepository usuarios;
    @Autowired
    private ReservaRepository reservas;
    @Autowired
    private JwtTokenProvider tokens;

    private String reservaHuesped1;

    @BeforeEach
    void buscarReservaDelHuesped1() {
        reservaHuesped1 = reservas.buscar(new FiltroReservas(null, null, null, "1094000001", null, null),
                PeticionPagina.primera()).contenido().getFirst().codigo().valor();
    }

    @Test
    @DisplayName("1-2: los cinco usuarios iniciales quedan en H2 con la contraseña en BCrypt")
    void usuariosInicialesConBcrypt() {
        assertThat(usuarios.count()).isGreaterThanOrEqualTo(5);
        assertThat(usuarios.findByCorreoIgnoreCase(RECEPCION)).get()
                .satisfies(u -> assertThat(u.getContrasena()).startsWith("{bcrypt}").doesNotContain("Recepcion2026"));
    }

    @Test
    @DisplayName("3: login correcto devuelve un token Bearer con el rol")
    void loginCorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(RECEPCION, "Recepcion2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("RECEPCIONISTA"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("4: contraseña errada responde 401 con mensaje genérico")
    void loginContrasenaErrada() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(RECEPCION, "Incorrecta1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas."));
    }

    @Test
    @DisplayName("5: correo sin formato responde 400")
    void loginCorreoSinFormato() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("no-es-correo", "Recepcion2026")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
    }

    @Test
    @DisplayName("6: sin token responde 401 con ErrorNegocioResponse")
    void sinToken() throws Exception {
        mvc.perform(get("/api/reservas").param("estado", "PENDIENTE"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    @DisplayName("7-8: el listado es solo para recepción y administración")
    void listadoPorRol() throws Exception {
        mvc.perform(get("/api/reservas").param("estado", "PENDIENTE").header(HttpHeaders.AUTHORIZATION,
                        bearer(HUESPED_1, "Huesped2026")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mvc.perform(get("/api/reservas").param("estado", "PENDIENTE").header(HttpHeaders.AUTHORIZATION,
                        bearer(RECEPCION, "Recepcion2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());
    }

    @Test
    @DisplayName("9-10: un huésped ve su reserva y no la de otro huésped")
    void propietarioDeLaReserva() throws Exception {
        mvc.perform(get("/api/reservas/{codigo}", reservaHuesped1)
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1, "Huesped2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(reservaHuesped1));
        mvc.perform(get("/api/reservas/{codigo}", reservaHuesped1)
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_2, "Huesped2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("11: una reserva inexistente es 404 para recepción y 403 para un huésped")
    void reservaInexistente() throws Exception {
        mvc.perform(get("/api/reservas/RES-NO-EXISTE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION, "Recepcion2026")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/reservas/RES-NO-EXISTE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1, "Huesped2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("12: el personal de servicio no confirma reservas")
    void servicioNoConfirma() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}/confirmar", reservaHuesped1)
                        .header(HttpHeaders.AUTHORIZATION, bearer(SERVICIO, "Servicio2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13: un token modificado a mano responde 401")
    void tokenAlterado() throws Exception {
        String token = token(RECEPCION, "Recepcion2026");
        String alterado = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");
        mvc.perform(get("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("14: un token vencido responde 401")
    void tokenVencido() throws Exception {
        Instant hace2Horas = Instant.now().minusSeconds(7200);
        String vencido = tokens.generateTokenAsString(RECEPCION, "1000000002", List.of("RECEPCIONISTA"),
                hace2Horas, hace2Horas.plusSeconds(1800));
        mvc.perform(get("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7.5.3: un huésped no puede crear una reserva a nombre de otra persona")
    void huespedSoloReservaASuNombre() throws Exception {
        String cuerpo = """
                {"identificacionApartamento":"SUQ-201","fechaEntrada":"2030-01-10","fechaSalida":"2030-01-13",
                 "titular":{"nombre":"Otra Persona","documentoIdentidad":"999999999","fechaNacimiento":"1990-01-01",
                            "correo":"otra@correo.co","telefono":"3000000000"}}
                """;
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(cuerpo)
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1, "Huesped2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7.5.2: registro público crea un HUESPED; el correo repetido es 409 y la clave débil 400")
    void registroPublico() throws Exception {
        String unico = UUID.randomUUID().toString().substring(0, 8);
        String cuerpo = """
                {"nombre":"Nuevo Huesped","correo":"%s@correo.co","contrasena":"Segura2026","telefono":"3001112233",
                 "documentoIdentidad":"%s","fechaNacimiento":"1995-05-05"}
                """.formatted(unico, unico);
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("HUESPED"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_YA_REGISTRADO"));
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.replace("Segura2026", "debil")))
                .andExpect(status().isBadRequest());
    }

    private String bearer(String correo, String contrasena) throws Exception {
        return "Bearer " + token(correo, contrasena);
    }

    private String token(String correo, String contrasena) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(correo, contrasena)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = json.readTree(respuesta);
        return nodo.get("token").asText();
    }

    private static String credenciales(String correo, String contrasena) {
        return "{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena);
    }
}

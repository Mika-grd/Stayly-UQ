package co.edu.uniquindio.sga.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Canal externo y conflictos (7.8) contra la aplicación completa: el administrador registra un canal,
 * el canal se autentica con su X-API-Key (no con JWT), reserva de forma idempotente (RN-19), choca con
 * una vigente (RN-18) y cancela; el administrador revisa la bitácora y resuelve el conflicto.
 * Los pasos dependen unos de otros, así que corren en orden sobre su propia base H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:canal;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CanalExternoIntegracionTest {

    private static final String ADMIN = "admin@stayly.local";
    private static final String RECEPCION = "recepcion@stayly.local";
    private static final String CLAVE_SIMULADOR = "sim-canal-uq-2026";
    private static final LocalDate ENTRADA = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(90);

    private static String clave;
    private static String codigoCreada;
    private static String idConflicto;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Test
    @Order(1)
    @DisplayName("7.8 · El administrador registra un canal; la credencial se muestra solo al crearlo")
    void registrarCanal() throws Exception {
        String admin = bearer(ADMIN);
        JsonNode canal = leer(mvc.perform(post("/api/canales").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Booking\",\"tipo\":\"EXTERNO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.credencialGenerada", startsWith("sga_"))));
        clave = canal.get("credencialGenerada").asText();

        mvc.perform(get("/api/canales").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Booking')].activo").value(true))
                .andExpect(jsonPath("$[?(@.nombre == 'Booking')].credencialGenerada").value(hasItem((Object) null)));
        mvc.perform(get("/api/canales").header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    @DisplayName("7.8 · /api/externo/** exige X-API-Key: sin ella, con una inválida o con un JWT es 401")
    void credencialDelCanal() throws Exception {
        String ruta = "/api/externo/disponibilidad?fechaEntrada={e}&fechaSalida={s}&fechasNacimiento=1990-01-01";
        mvc.perform(get(ruta, ENTRADA, ENTRADA.plusDays(2)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
        mvc.perform(get(ruta, ENTRADA, ENTRADA.plusDays(2)).header("X-API-Key", "sga_inventada"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(ruta, ENTRADA, ENTRADA.plusDays(2)).header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(ruta, ENTRADA, ENTRADA.plusDays(2)).header("X-API-Key", CLAVE_SIMULADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apartamentos.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(3)
    @DisplayName("CU-08, RN-19 · Una reserva nueva es 201 CREADA; el mismo mensaje otra vez es 200 YA_EXISTIA")
    void recibirReservaIdempotente() throws Exception {
        codigoCreada = leer(externa("BKG-1001")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("CREADA"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))).get("codigoReserva").asText();

        externa("BKG-1001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("YA_EXISTIA"))
                .andExpect(jsonPath("$.codigoReserva").value(codigoCreada));
        mvc.perform(get("/api/reservas/{codigo}", codigoCreada).header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canal").value("EXTERNO"));
    }

    @Test
    @Order(4)
    @DisplayName("RN-18 · Otra reserva sobre las mismas noches es 409 CONFLICTO_CANAL y queda el conflicto pendiente")
    void conflicto() throws Exception {
        externa("BKG-1002")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_CANAL"));

        JsonNode pagina = leer(mvc.perform(get("/api/conflictos-canal?estado=PENDIENTE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].canal").value("Booking"))
                .andExpect(jsonPath("$.contenido[0].identificadorExterno").value("BKG-1002"))
                .andExpect(jsonPath("$.contenido[0].codigoReservaVigente").value(codigoCreada)));
        idConflicto = pagina.at("/contenido/0/id").asText();
    }

    @Test
    @Order(5)
    @DisplayName("7.8 · El administrador resuelve el conflicto una vez; repetirlo es 409 y uno inexistente 404")
    void resolverConflicto() throws Exception {
        String admin = bearer(ADMIN);
        String cuerpo = "{\"resolucion\":\"Se avisó al canal y reubicó al huésped\"}";
        mvc.perform(put("/api/conflictos-canal/{id}/resolver", idConflicto).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"))
                .andExpect(jsonPath("$.resolucion").value("Se avisó al canal y reubicó al huésped"));
        mvc.perform(put("/api/conflictos-canal/{id}/resolver", idConflicto).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_YA_RESUELTO"));
        mvc.perform(put("/api/conflictos-canal/{id}/resolver", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_NO_ENCONTRADO"));
    }

    @Test
    @Order(6)
    @DisplayName("7.8 · Otro canal no cancela la reserva ajena (404); el canal dueño la cancela con su liquidación")
    void cancelar() throws Exception {
        mvc.perform(put("/api/externo/reservas/{id}/cancelar", "BKG-1001").header("X-API-Key", CLAVE_SIMULADOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NO_ENCONTRADA"));
        mvc.perform(put("/api/externo/reservas/{id}/cancelar", "BKG-1001").header("X-API-Key", clave))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value(codigoCreada))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
        mvc.perform(put("/api/externo/reservas/{id}/cancelar", "BKG-1001").header("X-API-Key", clave))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
    }

    @Test
    @Order(7)
    @DisplayName("7.8 · La bitácora del canal registra cada mensaje, del más reciente al más antiguo")
    void bitacora() throws Exception {
        mvc.perform(get("/api/canales/bitacora?canal=Booking").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].tipo").value("CANCELACION_RECIBIDA"))
                .andExpect(jsonPath("$.contenido[*].tipo").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "RESERVA_RECIBIDA", "RESERVA_DUPLICADA", "CONFLICTO_REGISTRADO", "CANCELACION_RECIBIDA")));
    }

    // Apoyo

    private ResultActions externa(String identificador) throws Exception {
        String cuerpo = """
                {"identificadorExterno":"%s","identificacionApartamento":"SUQ-201",
                 "fechaEntrada":"%s","fechaSalida":"%s","horaEstimadaLlegada":"16:00",
                 "titular":{"nombre":"Ana Rojas","documentoIdentidad":"1094555001","fechaNacimiento":"1991-04-10",
                            "correo":"ana.rojas@correo.co","telefono":"3005550001"}}
                """.formatted(identificador, ENTRADA, ENTRADA.plusDays(3));
        return mvc.perform(post("/api/externo/reservas").header("X-API-Key", clave)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private JsonNode leer(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }

    private String bearer(String correo) throws Exception {
        String contrasena = ADMIN.equals(correo) ? "Admin2026" : "Recepcion2026";
        JsonNode token = leer(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena)))
                .andExpect(status().isOk()));
        return "Bearer " + token.get("token").asText();
    }
}

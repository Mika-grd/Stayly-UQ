package co.edu.uniquindio.sga.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reportes contra la aplicación completa con los datos de prueba de 12.3: solo el administrador los ve. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reportes;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReportesIntegracionTest {

    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Test
    @DisplayName("Ocupación · Las noches de las reservas en curso y finalizadas de la carga cuentan como vendidas")
    void ocupacion() throws Exception {
        mvc.perform(get("/api/reportes/ocupacion?desde={d}&hasta={h}", HOY.minusDays(7), HOY.plusDays(14))
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin@stayly.local", "Admin2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nochesPeriodo").value(21))
                .andExpect(jsonPath("$.apartamentosActivos").value(greaterThan(0)))
                .andExpect(jsonPath("$.nochesVendidas").value(greaterThan(0)))
                .andExpect(jsonPath("$.porcentajeOcupacion").isNumber());
    }

    @Test
    @DisplayName("Ingresos y por canal · Traen los tres medios de pago y los tres canales")
    void ingresosYCanales() throws Exception {
        String admin = bearer("admin@stayly.local", "Admin2026");
        mvc.perform(get("/api/reportes/ingresos?desde={d}&hasta={h}", HOY.minusDays(30), HOY.plusDays(1))
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThan(0)))
                .andExpect(jsonPath("$.porMedioPago", hasSize(3)));
        mvc.perform(get("/api/reportes/reservas-por-canal?desde={d}&hasta={h}", HOY.minusDays(30), HOY.plusDays(1))
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porCanal", hasSize(3)))
                .andExpect(jsonPath("$.porCanal[?(@.canal == 'DIRECTO')].noShow").value(org.hamcrest.Matchers.hasItem(1)));
    }

    @Test
    @DisplayName("3.1 y 7.1 · Rango invertido 400, sin fechas 400, recepción 403")
    void errores() throws Exception {
        String admin = bearer("admin@stayly.local", "Admin2026");
        mvc.perform(get("/api/reportes/ingresos?desde={d}&hasta={h}", HOY, HOY).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RANGO_FECHAS_INVALIDO"));
        mvc.perform(get("/api/reportes/ocupacion").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
        mvc.perform(get("/api/reportes/reservas-por-canal?desde={d}&hasta={h}", HOY, HOY.plusDays(1))
                        .header(HttpHeaders.AUTHORIZATION, bearer("recepcion@stayly.local", "Recepcion2026")))
                .andExpect(status().isForbidden());
    }

    private String bearer(String correo, String contrasena) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(cuerpo).get("token").asText();
    }
}

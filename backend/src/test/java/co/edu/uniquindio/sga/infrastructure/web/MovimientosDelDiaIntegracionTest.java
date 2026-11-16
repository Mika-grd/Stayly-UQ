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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU-42 contra los datos de prueba (12.3): Carlos Mejía llega en 10 días (CONFIRMADA) y Sofía Herrera,
 * EN_CURSO en SUQ-202, sale en 2. La salida de Carlos aún no cuenta: su reserva no está en curso.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:movimientos;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MovimientosDelDiaIntegracionTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private final LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));

    @Test
    @DisplayName("CU-42 · Las llegadas son las reservas vigentes que entran ese día")
    void llegadasDelDia() throws Exception {
        mvc.perform(get("/api/reservas/movimientos-del-dia").param("fecha", hoy.plusDays(10).toString())
                        .header(HttpHeaders.AUTHORIZATION, recepcion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(hoy.plusDays(10).toString()))
                .andExpect(jsonPath("$.llegadas[*].identificacionApartamento").value(hasItem("SUQ-201")))
                .andExpect(jsonPath("$.salidas").isEmpty());
    }

    @Test
    @DisplayName("CU-42 · Las salidas son las reservas en curso que salen ese día")
    void salidasDelDia() throws Exception {
        mvc.perform(get("/api/reservas/movimientos-del-dia").param("fecha", hoy.plusDays(2).toString())
                        .header(HttpHeaders.AUTHORIZATION, recepcion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salidas[*].identificacionApartamento").value(hasItem("SUQ-202")));
        mvc.perform(get("/api/reservas/movimientos-del-dia").param("fecha", hoy.plusDays(13).toString())
                        .header(HttpHeaders.AUTHORIZATION, recepcion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salidas[*].identificacionApartamento").value(not(hasItem("SUQ-201"))));
    }

    private String recepcion() throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"recepcion@stayly.local\",\"contrasena\":\"Recepcion2026\"}"))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(cuerpo).get("token").asText();
    }
}

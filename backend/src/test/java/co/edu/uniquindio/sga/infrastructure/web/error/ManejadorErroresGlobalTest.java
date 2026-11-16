package co.edu.uniquindio.sga.infrastructure.web.error;

import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cada excepción llega al cliente como ErrorNegocioResponse con su HTTP, sin trazas técnicas. */
class ManejadorErroresGlobalTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private MockMvc mvc;

    @BeforeEach
    void montarControladorDePrueba() {
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 11, 2, 21, 0).atZone(BOGOTA).toInstant(), BOGOTA);
        mvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new ManejadorErroresGlobal(reloj))
                .build();
    }

    @Test
    @DisplayName("Conflicto con el estado actual · NOCHES_NO_DISPONIBLES responde 409 con la regla RN-01")
    void reglaDeConflictoResponde409() throws Exception {
        mvc.perform(get("/prueba/regla").param("codigo", "NOCHES_NO_DISPONIBLES"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NOCHES_NO_DISPONIBLES"))
                .andExpect(jsonPath("$.mensaje").value("Mensaje de negocio."))
                .andExpect(jsonPath("$.regla").value("RN-01"))
                .andExpect(jsonPath("$.ruta").value("/prueba/regla"))
                .andExpect(jsonPath("$.fecha").exists());
    }

    @Test
    @DisplayName("Regla sobre los datos · CAPACIDAD_EXCEDIDA responde 422 con la regla RN-02")
    void reglaSobreLosDatosResponde422() throws Exception {
        mvc.perform(get("/prueba/regla").param("codigo", "CAPACIDAD_EXCEDIDA"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CAPACIDAD_EXCEDIDA"))
                .andExpect(jsonPath("$.regla").value("RN-02"));
    }

    @Test
    @DisplayName("Recurso inexistente · RecursoNoEncontradoException responde 404 sin regla")
    void recursoNoEncontradoResponde404() throws Exception {
        mvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.mensaje").value("No existe la reserva RES-2026-09999."))
                .andExpect(jsonPath("$.regla").value(nullValue()));
    }

    @Test
    @DisplayName("Formato · Un DTO con un campo obligatorio faltante responde 400 FORMATO_INVALIDO")
    void dtoInvalidoResponde400() throws Exception {
        mvc.perform(post("/prueba/dto").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").value(startsWith("El campo fechaEntrada")));
    }

    @Test
    @DisplayName("Formato · Un JSON mal formado responde 400 FORMATO_INVALIDO")
    void jsonMalFormadoResponde400() throws Exception {
        mvc.perform(post("/prueba/dto").contentType(MediaType.APPLICATION_JSON).content("{\"fechaEntrada\": \"ayer\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
    }

    @Test
    @DisplayName("Formato · Un parámetro obligatorio faltante responde 400 con su nombre")
    void parametroFaltanteResponde400() throws Exception {
        mvc.perform(get("/prueba/regla"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").value("El parámetro codigo es obligatorio."));
    }

    @Test
    @DisplayName("Error inesperado · Responde 500 ERROR_INTERNO sin la traza técnica")
    void errorInesperadoNoExponeTraza() throws Exception {
        String cuerpo = mvc.perform(get("/prueba/fallo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andReturn().getResponse().getContentAsString();

        assertThat(cuerpo).doesNotContain("IllegalStateException", "detalle interno", "at co.edu");
    }

    record DtoDePrueba(@NotNull LocalDate fechaEntrada) {
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorDePrueba {

        @GetMapping("/regla")
        void regla(@RequestParam String codigo) {
            throw new ReglaDominioException(codigo, "Mensaje de negocio.");
        }

        @GetMapping("/no-encontrado")
        void noEncontrado() {
            throw new RecursoNoEncontradoException("RESERVA_NO_ENCONTRADA", "No existe la reserva RES-2026-09999.");
        }

        @PostMapping("/dto")
        void dto(@Valid @RequestBody DtoDePrueba dto) {
        }

        @GetMapping("/fallo")
        void fallo() {
            throw new IllegalStateException("detalle interno");
        }
    }
}

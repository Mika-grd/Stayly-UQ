package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Folio (7.7) contra la aplicación completa con login real: los dos recorridos que el folio completa
 * (pagar el anticipo para confirmar; cerrar el folio y registrar la salida hasta FINALIZADA), el cierre
 * con saldo (RN-17) y quién ve qué. Usa su propia base H2 y cierra su contexto, porque cambia datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:folio;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FolioIntegracionTest {

    private static final String ADMIN = "admin@stayly.local";
    private static final String RECEPCION = "recepcion@stayly.local";
    private static final String HUESPED_1 = "huesped1@stayly.local";   // titular 1094000001 (Laura Gómez)

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private ReservaRepository reservas;

    private String pendienteLaura;
    private String confirmadaCarlos;
    private String enCursoSofia;

    @BeforeEach
    void buscarReservasDeLaCarga() {
        pendienteLaura = reservaDe("1094000001");
        confirmadaCarlos = reservaDe("1094000002");
        enCursoSofia = reservaDe("1094000003");
    }

    @Test
    @DisplayName("CU-07 y CU-03 · Sin anticipo no confirma; tras pagar el 30 % confirma sin tocar la configuración")
    void pagarAnticipoYConfirmar() throws Exception {
        String recepcion = bearer(RECEPCION);
        perform(put("/api/reservas/{codigo}/hora-llegada", pendienteLaura), recepcion, "{\"horaEstimadaLlegada\":\"16:00\"}")
                .andExpect(status().isOk());
        perform(put("/api/reservas/{codigo}/confirmar", pendienteLaura), recepcion, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ANTICIPO_INSUFICIENTE"));

        long total = leer(perform(get("/api/reservas/{codigo}", pendienteLaura), recepcion, null)
                .andExpect(status().isOk())).at("/valor/total").asLong();
        long anticipo = (total * 30 + 99) / 100;
        perform(post("/api/reservas/{codigo}/folio/pagos", pendienteLaura), recepcion, pago(anticipo, "TRANSFERENCIA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPagos").value(anticipo))
                .andExpect(jsonPath("$.pagos", hasSize(1)))
                .andExpect(jsonPath("$.pagos[0].medio").value("TRANSFERENCIA"));

        perform(put("/api/reservas/{codigo}/confirmar", pendienteLaura), recepcion, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("CU-18 y CU-06 · Con saldo no cierra; pagado el saldo se cierra el folio y la salida deja FINALIZADA")
    void cerrarFolioYRegistrarSalida() throws Exception {
        String recepcion = bearer(RECEPCION);
        perform(post("/api/reservas/{codigo}/folio/cargos", enCursoSofia), recepcion,
                "{\"tipo\":\"SERVICIO_ADICIONAL\",\"concepto\":\"Parqueadero 2 noches\",\"valor\":30000}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cargos[-1:].tipo").value("SERVICIO_ADICIONAL"));
        perform(put("/api/reservas/{codigo}/registrar-salida", enCursoSofia), recepcion, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FOLIO_ABIERTO"));
        perform(put("/api/reservas/{codigo}/folio/cerrar", enCursoSofia), recepcion, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FOLIO_CON_SALDO_SIN_AUTORIZACION"));

        long saldo = leer(perform(get("/api/reservas/{codigo}/folio", enCursoSofia), recepcion, null)
                .andExpect(status().isOk())).get("saldo").asLong();
        perform(post("/api/reservas/{codigo}/folio/pagos", enCursoSofia), recepcion, pago(saldo, "TARJETA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(0));
        perform(put("/api/reservas/{codigo}/folio/cerrar", enCursoSofia), recepcion, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"))
                .andExpect(jsonPath("$.autorizacionCierre").doesNotExist());

        perform(put("/api/reservas/{codigo}/registrar-salida", enCursoSofia), recepcion, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
        perform(post("/api/reservas/{codigo}/folio/pagos", enCursoSofia), recepcion, pago(1000, "EFECTIVO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FOLIO_CERRADO"));
    }

    @Test
    @DisplayName("RN-17 · Recepción no cierra con saldo aunque traiga motivo; el administrador sí, y queda su autorización")
    void cerrarConSaldoSoloAdministrador() throws Exception {
        String motivo = "{\"motivoAutorizacion\":\"Cortesía por cambio de apartamento\"}";
        perform(put("/api/reservas/{codigo}/folio/cerrar", confirmadaCarlos), bearer(RECEPCION), motivo)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FOLIO_CON_SALDO_SIN_AUTORIZACION"));
        perform(put("/api/reservas/{codigo}/folio/cerrar", confirmadaCarlos), bearer(ADMIN), motivo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"))
                .andExpect(jsonPath("$.autorizacionCierre.autor").value(ADMIN))
                .andExpect(jsonPath("$.autorizacionCierre.motivo").value("Cortesía por cambio de apartamento"));
    }

    @Test
    @DisplayName("CU-19 · El huésped ve el folio de su reserva pero no el ajeno, ni registra movimientos")
    void accesoDelHuesped() throws Exception {
        String huesped = bearer(HUESPED_1);
        perform(get("/api/reservas/{codigo}/folio", pendienteLaura), huesped, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value(pendienteLaura))
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.cargos[0].tipo").value("ALOJAMIENTO"));
        perform(get("/api/reservas/{codigo}/folio", confirmadaCarlos), huesped, null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        perform(post("/api/reservas/{codigo}/folio/pagos", pendienteLaura), huesped, pago(1000, "EFECTIVO"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7.7 · Errores: reserva inexistente 404, cargo de tipo del sistema 400, valor cero 409 VALOR_INVALIDO")
    void errores() throws Exception {
        String recepcion = bearer(RECEPCION);
        perform(get("/api/reservas/{codigo}/folio", "RES-2026-99999"), recepcion, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NO_ENCONTRADA"));
        perform(post("/api/reservas/{codigo}/folio/cargos", confirmadaCarlos), recepcion,
                "{\"tipo\":\"PENALIDAD\",\"concepto\":\"Multa\",\"valor\":10000}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
        perform(post("/api/reservas/{codigo}/folio/pagos", confirmadaCarlos), recepcion, pago(0, "EFECTIVO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VALOR_INVALIDO"));
    }

    // Apoyo

    private ResultActions perform(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        peticion.header(HttpHeaders.AUTHORIZATION, token);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        return mvc.perform(peticion);
    }

    private JsonNode leer(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }

    private static String pago(long valor, String medio) {
        return "{\"valor\":%d,\"medio\":\"%s\",\"fecha\":\"%s\",\"referencia\":\"REF-%d\"}"
                .formatted(valor, medio, LocalDateTime.now().withNano(0), valor);
    }

    private String reservaDe(String documentoTitular) {
        return reservas.buscar(new FiltroReservas(null, null, null, documentoTitular, null, null),
                PeticionPagina.primera()).contenido().getFirst().codigo().valor();
    }

    private String bearer(String correo) throws Exception {
        String contrasena = switch (correo) {
            case ADMIN -> "Admin2026";
            case RECEPCION -> "Recepcion2026";
            default -> "Huesped2026";
        };
        ResultActions login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena)));
        return "Bearer " + leer(login.andExpect(status().isOk())).get("token").asText();
    }
}

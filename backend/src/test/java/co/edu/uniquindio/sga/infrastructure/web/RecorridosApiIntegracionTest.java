package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
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

import java.time.LocalDate;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorridos expuestos en la Entrega 2 tras la retroalimentación: modificar la reserva (CU-13), la
 * hora estimada de llegada (CU-14), el no-show (CU-12) y la configuración del alojamiento (CU-36,
 * CU-37), contra la aplicación completa con login real. Usa su propia base H2 y cierra su contexto al
 * terminar, porque cambia datos y la configuración vigente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:recorridos;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RecorridosApiIntegracionTest {

    private static final String ADMIN = "admin@stayly.local";
    private static final String RECEPCION = "recepcion@stayly.local";
    private static final String HUESPED_1 = "huesped1@stayly.local";   // titular 1094000001 (Laura Gómez)
    private static final String HUESPED_2 = "huesped2@stayly.local";   // titular 1094000002 (Carlos Mejía)

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private ReservaRepository reservas;

    private final LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));

    private String pendienteLaura;
    private String confirmadaCarlos;
    private String confirmadaSinLlegada;
    private String cancelada;

    @BeforeEach
    void buscarReservasDeLaCarga() {
        pendienteLaura = reservaDe("1094000001");
        confirmadaCarlos = reservaDe("1094000002");
        cancelada = reservaDe("1094000005");
        confirmadaSinLlegada = reservaDe("1094000007");
    }

    // CU-13 Modificar reserva

    @Test
    @DisplayName("CU-13 · Recepción modifica las fechas de una reserva PENDIENTE y recibe 200 con la reserva nueva")
    void modificarReservaPendiente() throws Exception {
        LocalDate entrada = hoy.plusDays(21);
        mvc.perform(put("/api/reservas/{codigo}", pendienteLaura).header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content(modificacion("SUQ-101", entrada, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(pendienteLaura))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaEntrada").value(entrada.toString()))
                .andExpect(jsonPath("$.numeroNoches").value(3));
    }

    @Test
    @DisplayName("CU-13 · Modificar una reserva CANCELADA responde 409 RESERVA_NO_MODIFICABLE (RN-14)")
    void modificarReservaCancelada() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}", cancelada).header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content(modificacion("SUQ-102", hoy.plusDays(40), 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NO_MODIFICABLE"))
                .andExpect(jsonPath("$.regla").value("RN-14"));
    }

    @Test
    @DisplayName("CU-13 · Un huésped no puede modificar (403) y un cuerpo sin fechas es 400")
    void modificarSinPermisoOConCuerpoInvalido() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}", pendienteLaura).header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1))
                        .contentType(MediaType.APPLICATION_JSON).content(modificacion("SUQ-101", hoy.plusDays(21), 3)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/reservas/{codigo}", pendienteLaura).header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"identificacionApartamento\":\"SUQ-101\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
    }

    @Test
    @DisplayName("CU-13 · Pasar la reserva a un apartamento de 2 personas con 3 ocupantes es 409 CAPACIDAD_EXCEDIDA")
    void modificarConCapacidadExcedida() throws Exception {
        String tresOcupantes = """
                {"identificacionApartamento":"SUQ-102","fechaEntrada":"%s","fechaSalida":"%s",
                 "titular":{"nombre":"Laura Gómez","documentoIdentidad":"1094000001","fechaNacimiento":"1990-03-14",
                            "correo":"laura.gomez@correo.co","telefono":"3004000001"},
                 "acompanantes":[{"nombre":"Uno","fechaNacimiento":"1991-01-01"},
                                 {"nombre":"Dos","fechaNacimiento":"1992-01-01"}]}
                """.formatted(hoy.plusDays(50), hoy.plusDays(52));
        mvc.perform(put("/api/reservas/{codigo}", pendienteLaura).header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content(tresOcupantes))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CAPACIDAD_EXCEDIDA"))
                .andExpect(jsonPath("$.regla").value("RN-02"));
    }

    // CU-14 Hora estimada de llegada

    @Test
    @DisplayName("CU-14 · El titular indica la hora de llegada de su reserva; otro huésped recibe 403")
    void indicarHoraEstimadaLlegada() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}/hora-llegada", pendienteLaura)
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"horaEstimadaLlegada\":\"17:30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaEstimadaLlegada").value("17:30:00"));
        mvc.perform(put("/api/reservas/{codigo}/hora-llegada", pendienteLaura)
                        .header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_2))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"horaEstimadaLlegada\":\"18:00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CU-14 · Indicar la hora de una reserva CANCELADA responde 409 RESERVA_NO_MODIFICABLE")
    void indicarHoraEnReservaCancelada() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}/hora-llegada", cancelada)
                        .header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"horaEstimadaLlegada\":\"18:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NO_MODIFICABLE"));
    }

    // CU-12 No-show

    @Test
    @DisplayName("CU-12 · La reserva confirmada cuya entrada fue ayer pasa a NO_SHOW con la liquidación")
    void declararNoShow() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}/no-show", confirmadaSinLlegada)
                        .header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value(confirmadaSinLlegada))
                .andExpect(jsonPath("$.estado").value("NO_SHOW"))
                .andExpect(jsonPath("$.retencion").isNumber());
    }

    @Test
    @DisplayName("CU-12 · Antes de la hora límite del día de entrada responde 409 NO_SHOW_ANTES_DE_HORA_LIMITE")
    void noShowAntesDeLaHoraLimite() throws Exception {
        mvc.perform(put("/api/reservas/{codigo}/no-show", confirmadaCarlos)
                        .header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NO_SHOW_ANTES_DE_HORA_LIMITE"))
                .andExpect(jsonPath("$.regla").value("L-15"));
    }

    // CU-36 y CU-37 Configuración del alojamiento

    @Test
    @DisplayName("CU-36 · El administrador cambia los datos y parámetros del alojamiento; recepción recibe 403")
    void configurarAlojamiento() throws Exception {
        mvc.perform(put("/api/alojamiento").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(alojamiento("15:00", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Stayly-UQ Apart-Hotel"))
                .andExpect(jsonPath("$.parametros.tiempoPreparacionHoras").value(2))
                .andExpect(jsonPath("$.reglasPropias.cupoMascotas").isNumber());
        mvc.perform(put("/api/alojamiento").header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION))
                        .contentType(MediaType.APPLICATION_JSON).content(alojamiento("15:00", 3)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CU-36 · Una entrada antes de la salida es 409 PARAMETRO_INVALIDO; sin nombre es 400")
    void configurarAlojamientoInvalido() throws Exception {
        mvc.perform(put("/api/alojamiento").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(alojamiento("10:00", 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
        mvc.perform(put("/api/alojamiento").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ciudad\":\"Armenia\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_INVALIDO"));
    }

    @Test
    @DisplayName("CU-37 · El administrador cambia RP-02 y RP-03; un cupo negativo es 409 PARAMETRO_INVALIDO")
    void configurarParametrosReglasPropias() throws Exception {
        mvc.perform(put("/api/alojamiento/parametros").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(reglasPropias(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reglasPropias.cupoMascotas").value(3))
                .andExpect(jsonPath("$.reglasPropias.horaInicioLlegadaNocturna").value("22:00:00"))
                .andExpect(jsonPath("$.reglasPropias.valorRecargoLlegadaNocturna").value(35000));
        mvc.perform(put("/api/alojamiento/parametros").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(reglasPropias(-1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    // Mis reservas (portal)

    @Test
    @DisplayName("7.2 · El huésped ve solo las reservas de las que es titular; recepción no usa esta lista")
    void misReservas() throws Exception {
        mvc.perform(get("/api/usuarios/me/reservas").header(HttpHeaders.AUTHORIZATION, bearer(HUESPED_1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].codigo").value(org.hamcrest.Matchers.hasItem(pendienteLaura)))
                .andExpect(jsonPath("$.contenido[*].codigo").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(confirmadaCarlos))));
        mvc.perform(get("/api/usuarios/me/reservas").header(HttpHeaders.AUTHORIZATION, bearer(RECEPCION)))
                .andExpect(status().isForbidden());
    }

    // Calendario de disponibilidad del detalle (público)

    @Test
    @DisplayName("7.1 · El calendario de disponibilidad es público y no expone códigos de reserva")
    void calendarioDisponibilidadPublico() throws Exception {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Bogota"));
        mvc.perform(get("/api/apartamentos/SUQ-101/disponibilidad")
                        .param("desde", hoy.toString()).param("hasta", hoy.plusDays(14).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("SUQ-101"))
                .andExpect(jsonPath("$.noches.length()").value(14))
                .andExpect(jsonPath("$.noches[0].disponible").isBoolean())
                .andExpect(jsonPath("$.noches[0].codigoReserva").doesNotExist());
        mvc.perform(get("/api/apartamentos/SUQ-101/disponibilidad")
                        .param("desde", hoy.toString()).param("hasta", hoy.plusYears(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RANGO_FECHAS_INVALIDO"));
    }

    // Apoyo

    private String reservaDe(String documentoTitular) {
        return reservas.buscar(new FiltroReservas(null, null, null, documentoTitular, null, null),
                PeticionPagina.primera()).contenido().getFirst().codigo().valor();
    }

    private String modificacion(String apartamento, LocalDate entrada, int noches) {
        return """
                {"identificacionApartamento":"%s","fechaEntrada":"%s","fechaSalida":"%s",
                 "titular":{"nombre":"Laura Gómez","documentoIdentidad":"1094000001","fechaNacimiento":"1990-03-14",
                            "correo":"laura.gomez@correo.co","telefono":"3004000001"}}
                """.formatted(apartamento, entrada, entrada.plusDays(noches));
    }

    private static String alojamiento(String horaEntrada, int tiempoPreparacion) {
        return """
                {"nombre":"Stayly-UQ Apart-Hotel","descripcion":"Seis apartamentos en Armenia","ciudad":"Armenia",
                 "direccion":"Carrera 15 con Calle 12 Norte","ubicacion":{"latitud":4.5532,"longitud":-75.6593},
                 "horaEntrada":"%s","horaSalida":"12:00","normasConvivencia":["Silencio de 22:00 a 7:00"],
                 "serviciosAdicionales":[{"nombre":"Parqueadero","generaCargo":true,"valor":15000}],
                 "parametros":{"umbralEdadFacturable":12,"tiempoPreparacionHoras":%d,"plazoConfirmacionHoras":24,
                               "horaLimiteNoShow":"23:00","exigeAnticipo":true,"porcentajeAnticipo":30,
                               "mediosPagoAceptados":["EFECTIVO","TRANSFERENCIA","TARJETA"]}}
                """.formatted(horaEntrada, tiempoPreparacion);
    }

    private static String reglasPropias(int cupo) {
        return """
                {"cupoMascotas":%d,"cargoPorMascotaNoche":25000,"horaInicioLlegadaNocturna":"22:00",
                 "valorRecargoLlegadaNocturna":35000}
                """.formatted(cupo);
    }

    private String bearer(String correo) throws Exception {
        String contrasena = switch (correo) {
            case ADMIN -> "Admin2026";
            case RECEPCION -> "Recepcion2026";
            default -> "Huesped2026";
        };
        ResultActions login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, contrasena)));
        String cuerpo = login.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(cuerpo).get("token").asText();
    }
}

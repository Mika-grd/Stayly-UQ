package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.TipoCargo;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.reserva.DatosContactoTitular;
import co.edu.uniquindio.sga.domain.model.reserva.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.IdOcupante;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.VerificadorEstanciaMinimaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CU-02. Orquesta la creación en el orden de 7.5 y las reglas propias RP-01, RP-02 y RP-03.
 * No contiene reglas: carga agregados, llama al dominio y a los servicios, y guarda.
 */
public class CrearReservaUseCase {

    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final PoliticaCancelacionRepository politicas;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final VerificadorEstanciaMinimaService verificadorEstanciaMinima;
    private final AdmisionMascotasService admisionMascotas;
    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public CrearReservaUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas, FolioRepository folios,
                               PoliticaCancelacionRepository politicas,
                               VerificadorTarifasCompletasService verificadorTarifas,
                               VerificadorEstanciaMinimaService verificadorEstanciaMinima,
                               AdmisionMascotasService admisionMascotas, DisponibilidadApartamentoService disponibilidad,
                               CotizadorEstanciaService cotizador, ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.folios = folios;
        this.politicas = politicas;
        this.verificadorTarifas = verificadorTarifas;
        this.verificadorEstanciaMinima = verificadorEstanciaMinima;
        this.admisionMascotas = admisionMascotas;
        this.disponibilidad = disponibilidad;
        this.cotizador = cotizador;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public ReservaCreada ejecutar(Comando comando) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Estancia estancia = new Estancia(comando.entrada(), comando.salida());
        Apartamento apartamento = apartamentos.buscarPorIdentificacion(comando.apartamento())
                .orElseThrow(() -> new ReglaDominioException("APARTAMENTO_NO_ENCONTRADO",
                        "No existe el apartamento " + comando.apartamento() + "."));
        List<Ocupante> ocupantes = comando.ocupantes().stream().map(DatosOcupante::aOcupante).toList();
        MascotasAutorizadas mascotas = new MascotasAutorizadas(comando.mascotas());
        HoraEstimadaLlegada hora = comando.horaEstimadaLlegada() == null
                ? null : new HoraEstimadaLlegada(comando.horaEstimadaLlegada());

        verificadorTarifas.verificarTarifasCompletas(apartamento.identificacion());
        verificadorEstanciaMinima.verificarEstanciaMinima(estancia);
        admisionMascotas.verificarMascotas(apartamento, mascotas);
        disponibilidad.verificarDisponible(apartamento, estancia, ocupantes.size(), null);

        Reserva reserva = Reserva.crear(reservas.siguienteCodigo(), apartamento.identificacion(), estancia,
                ocupantes, comando.canal(), comando.identificadorExterno(), hora, mascotas,
                cotizarCon(apartamento, estancia, ocupantes), politicas.buscarVigente().version(),
                configuracion.umbralEdad(), comando.autor(), ahora);

        Folio folio = Folio.abrir(new IdFolio(UUID.randomUUID()), reserva.codigo(), reserva.valor().total(), ahora);
        if (mascotas.hayMascotas()) {
            folio.registrarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL, "Cargo por mascotas",
                    admisionMascotas.cargoPorMascotas(mascotas, estancia), ahora));
        }
        configuracion.recargoLlegadaNocturna().cargoPorCambioDeHora(null, hora, ahora).ifPresent(folio::registrarCargo);

        reservas.guardar(reserva);
        folios.guardar(folio);
        return new ReservaCreada(reserva, folio, apartamento.advertenciaAcceso());
    }

    private ValorCongelado cotizarCon(Apartamento apartamento, Estancia estancia, List<Ocupante> ocupantes) {
        return cotizador.cotizar(apartamento.identificacion(), estancia,
                new ComposicionGrupo(ocupantes.stream().map(Ocupante::fechaNacimiento).toList()));
    }

    public record Comando(IdentificacionApartamento apartamento, LocalDate entrada, LocalDate salida,
                          List<DatosOcupante> ocupantes, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                          LocalTime horaEstimadaLlegada, int mascotas, String autor) {
    }

    /** Datos de un ocupante tal como llegan; el titular trae documento y contacto. */
    public record DatosOcupante(String nombre, LocalDate fechaNacimiento, String documento, boolean titular,
                                String correo, String telefono) {

        Ocupante aOcupante() {
            IdOcupante id = new IdOcupante(UUID.randomUUID());
            DocumentoIdentidad doc = documento == null ? null : new DocumentoIdentidad(documento);
            return titular
                    ? Ocupante.titular(id, nombre, fechaNacimiento, doc, new DatosContactoTitular(correo, telefono))
                    : Ocupante.acompanante(id, nombre, fechaNacimiento, doc);
        }
    }

    /** La reserva nace PENDIENTE; la advertencia de acceso se muestra al titular antes de confirmar. */
    public record ReservaCreada(Reserva reserva, Folio folio, Optional<String> advertenciaAcceso) {
    }
}

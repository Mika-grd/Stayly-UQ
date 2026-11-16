package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.Canal;
import co.edu.uniquindio.sga.domain.model.canal.ConflictoCanal;
import co.edu.uniquindio.sga.domain.model.canal.EventoCanal;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.canal.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.model.canal.TipoEventoCanal;
import co.edu.uniquindio.sga.domain.model.reserva.CanalOrigen;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * CU-08 (7.8): ConciliacionCanalExternoService decide si el mensaje es nuevo, repetido (RN-19) o choca
 * con una reserva vigente (RN-18). Si es nueva sigue el flujo de CU-02 con canal EXTERNO; si choca, la
 * vigente no se toca y queda un conflicto para el administrador. Todo queda en la bitácora del canal.
 * El conflicto se devuelve como resultado, no como excepción, para que se guarde con la transacción.
 */
@Service
@Transactional
public class RecibirReservaExternaUseCase {

    private final CanalRepository canales;
    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;
    private final ConflictoCanalRepository conflictos;
    private final ConciliacionCanalExternoService conciliacion;
    private final CrearReservaUseCase crear;
    private final Clock reloj;

    public RecibirReservaExternaUseCase(CanalRepository canales, ApartamentoRepository apartamentos,
                                        ReservaRepository reservas, ConflictoCanalRepository conflictos,
                                        ConciliacionCanalExternoService conciliacion, CrearReservaUseCase crear,
                                        Clock reloj) {
        this.canales = canales;
        this.apartamentos = apartamentos;
        this.reservas = reservas;
        this.conflictos = conflictos;
        this.conciliacion = conciliacion;
        this.crear = crear;
        this.reloj = reloj;
    }

    public Recepcion ejecutar(IdCanal idCanal, Comando comando) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Canal canal = Consultas.canal(canales, idCanal);
        IdentificadorExterno identificador = new IdentificadorExterno(canal.id(), comando.identificadorExterno());
        Apartamento apartamento = Consultas.apartamento(apartamentos, comando.apartamento());
        Estancia estancia = new Estancia(comando.entrada(), comando.salida());

        ResultadoConciliacion resultado = conciliacion.conciliar(identificador, apartamento, estancia,
                comando.ocupantes().size(), ahora);
        Recepcion recepcion = switch (resultado.tipo()) {
            case YA_EXISTE -> {
                Reserva existente = Consultas.reserva(reservas, resultado.reservaExistente());
                anotar(canal, TipoEventoCanal.RESERVA_DUPLICADA, identificador, ahora,
                        "YA_EXISTIA " + existente.codigo().valor());
                yield new Recepcion(TipoRecepcion.YA_EXISTIA, existente, null);
            }
            case CONFLICTO -> {
                ConflictoCanal conflicto = resultado.conflicto();
                conflictos.guardar(conflicto);
                anotar(canal, TipoEventoCanal.CONFLICTO_REGISTRADO, identificador, ahora,
                        "RECHAZADA: choca con " + conflicto.reservaVigente().valor());
                yield new Recepcion(TipoRecepcion.CONFLICTO, null, conflicto);
            }
            case ACEPTAR -> {
                Reserva creada = crear.ejecutar(new CrearReservaUseCase.Comando(apartamento.identificacion(),
                        comando.entrada(), comando.salida(), comando.ocupantes(), CanalOrigen.EXTERNO, identificador,
                        comando.horaEstimadaLlegada(), comando.mascotas(), canal.nombre())).reserva();
                anotar(canal, TipoEventoCanal.RESERVA_RECIBIDA, identificador, ahora,
                        "CREADA " + creada.codigo().valor());
                yield new Recepcion(TipoRecepcion.CREADA, creada, null);
            }
        };
        canales.guardar(canal);
        return recepcion;
    }

    private static void anotar(Canal canal, TipoEventoCanal tipo, IdentificadorExterno identificador,
                               LocalDateTime ahora, String resultado) {
        canal.registrarEvento(new EventoCanal(tipo.name(), identificador.codigo(), ahora, resultado));
    }

    public record Comando(String identificadorExterno, IdentificacionApartamento apartamento, LocalDate entrada,
                          LocalDate salida, LocalTime horaEstimadaLlegada,
                          List<CrearReservaUseCase.DatosOcupante> ocupantes, int mascotas) {
    }

    public enum TipoRecepcion {
        CREADA,
        YA_EXISTIA,
        CONFLICTO
    }

    /** CREADA o YA_EXISTIA traen la reserva; CONFLICTO trae el conflicto registrado. */
    public record Recepcion(TipoRecepcion tipo, Reserva reserva, ConflictoCanal conflicto) {
    }
}

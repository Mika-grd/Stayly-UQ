package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.request.ReservaExternaRequest;
import co.edu.uniquindio.sga.application.dto.response.DisponibilidadExternaResponse;
import co.edu.uniquindio.sga.application.dto.response.EstadoLiquidacion;
import co.edu.uniquindio.sga.application.dto.response.LiquidacionReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaExternaResponse;
import co.edu.uniquindio.sga.application.dto.response.ResultadoReservaExterna;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaExternaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadExternaUseCase;
import co.edu.uniquindio.sga.application.usecase.RecibirReservaExternaUseCase;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.infrastructure.web.mapper.ApartamentoWebMapper;
import co.edu.uniquindio.sga.infrastructure.web.mapper.CanalWebMapper;
import co.edu.uniquindio.sga.infrastructure.web.mapper.ReservaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del canal externo (10.3.1, 7.8). Lo autentica FiltroCredencialCanal con X-API-Key; el canal
 * autenticado es el principal, así cada canal solo ve y cancela las reservas que él envió.
 */
@RestController
public class CanalExternoController {

    private final ConsultarDisponibilidadExternaUseCase disponibilidad;
    private final RecibirReservaExternaUseCase recibir;
    private final CancelarReservaExternaUseCase cancelar;
    private final CanalWebMapper mapper;
    private final ApartamentoWebMapper apartamentoMapper;
    private final ReservaWebMapper reservaMapper;

    public CanalExternoController(ConsultarDisponibilidadExternaUseCase disponibilidad,
                                  RecibirReservaExternaUseCase recibir, CancelarReservaExternaUseCase cancelar,
                                  CanalWebMapper mapper, ApartamentoWebMapper apartamentoMapper,
                                  ReservaWebMapper reservaMapper) {
        this.disponibilidad = disponibilidad;
        this.recibir = recibir;
        this.cancelar = cancelar;
        this.mapper = mapper;
        this.apartamentoMapper = apartamentoMapper;
        this.reservaMapper = reservaMapper;
    }

    @GetMapping("/api/externo/disponibilidad")
    public DisponibilidadExternaResponse disponibilidad(@AuthenticationPrincipal IdCanal canal,
                                                        @RequestParam LocalDate fechaEntrada,
                                                        @RequestParam LocalDate fechaSalida,
                                                        @RequestParam List<LocalDate> fechasNacimiento) {
        var apartamentos = disponibilidad.ejecutar(canal, fechaEntrada, fechaSalida, fechasNacimiento).stream()
                .map(apartamentoMapper::aResumen)
                .toList();
        return new DisponibilidadExternaResponse(fechaEntrada, fechaSalida, apartamentos);
    }

    /** CU-08: 201 si la crea, 200 si el mensaje se repite (RN-19), 409 CONFLICTO_CANAL si choca (RN-18). */
    @PostMapping("/api/externo/reservas")
    public ResponseEntity<ReservaExternaResponse> recibir(@AuthenticationPrincipal IdCanal canal,
                                                          @Valid @RequestBody ReservaExternaRequest request) {
        var comando = new RecibirReservaExternaUseCase.Comando(request.identificadorExterno(),
                new IdentificacionApartamento(request.identificacionApartamento()), request.fechaEntrada(),
                request.fechaSalida(), request.horaEstimadaLlegada(),
                reservaMapper.aDatosOcupantes(request.titular(), request.acompanantes()),
                request.mascotasAutorizadas() == null ? 0 : request.mascotasAutorizadas());
        RecibirReservaExternaUseCase.Recepcion recepcion = recibir.ejecutar(canal, comando);
        return switch (recepcion.tipo()) {
            case CREADA -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.aReservaExterna(
                    request.identificadorExterno(), recepcion.reserva(), ResultadoReservaExterna.CREADA));
            case YA_EXISTIA -> ResponseEntity.ok(mapper.aReservaExterna(
                    request.identificadorExterno(), recepcion.reserva(), ResultadoReservaExterna.YA_EXISTIA));
            // El conflicto ya quedó guardado: la excepción solo produce el 409 del contrato.
            case CONFLICTO -> throw new ReglaDominioException("CONFLICTO_CANAL",
                    "La reserva choca con la reserva vigente " + recepcion.conflicto().reservaVigente().valor()
                            + "; se registró el conflicto para el administrador.");
        };
    }

    @PutMapping("/api/externo/reservas/{identificadorExterno}/cancelar")
    public LiquidacionReservaResponse cancelar(@AuthenticationPrincipal IdCanal canal,
                                               @PathVariable String identificadorExterno) {
        CancelarReservaExternaUseCase.Cancelacion cancelacion = cancelar.ejecutar(canal, identificadorExterno);
        return reservaMapper.aLiquidacion(cancelacion.reserva(), cancelacion.folio(), EstadoLiquidacion.CANCELADA);
    }
}

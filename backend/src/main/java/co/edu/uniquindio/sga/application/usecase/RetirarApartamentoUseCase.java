package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RetiroApartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * 7.3: retira el apartamento de la venta (eliminación lógica). RetiroApartamentoService lo
 * rechaza si tiene reservas activas o futuras. Las reservas históricas lo siguen referenciando.
 */
@Service
@Transactional
public class RetirarApartamentoUseCase {

    private final ApartamentoRepository apartamentos;
    private final RetiroApartamentoService retiro;
    private final Clock reloj;

    public RetirarApartamentoUseCase(ApartamentoRepository apartamentos, RetiroApartamentoService retiro,
                                     Clock reloj) {
        this.apartamentos = apartamentos;
        this.retiro = retiro;
        this.reloj = reloj;
    }

    public void ejecutar(IdentificacionApartamento identificacion) {
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        retiro.retirarDeLaVenta(apartamento, LocalDate.now(reloj));
        apartamentos.guardar(apartamento);
    }
}

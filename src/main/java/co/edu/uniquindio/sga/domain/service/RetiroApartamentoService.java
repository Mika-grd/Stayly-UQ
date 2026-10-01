package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.LocalDate;

/** 7.3: solo se retira de la venta (eliminación lógica) si no tiene reservas activas ni futuras. */
public class RetiroApartamentoService {

    private final ReservaRepository reservas;

    public RetiroApartamentoService(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public void retirarDeLaVenta(Apartamento apartamento, LocalDate hoy) {
        if (reservas.tieneActivasOFuturas(apartamento.identificacion(), hoy)) {
            throw new ReglaDominioException("APARTAMENTO_CON_RESERVAS_ACTIVAS",
                    "El apartamento tiene reservas activas o futuras.");
        }
        apartamento.retirar();
    }
}

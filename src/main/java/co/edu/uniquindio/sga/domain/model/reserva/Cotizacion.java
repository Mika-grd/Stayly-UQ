package co.edu.uniquindio.sga.domain.model.reserva;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;

/**
 * Objeto de valor Cotización (hoja Identificación): el precio de una estancia antes de reservar,
 * con su desglose noche por noche. No se guarda: al crear la reserva el desglose se convierte en
 * su ValorCongelado (RN-22).
 */
public record Cotizacion(IdentificacionApartamento apartamento, Estancia estancia, ValorCongelado valor,
                         int ocupantesTotales, int ocupantesFacturables, Dinero cargoMascotas) {

    public Cotizacion {
        if (apartamento == null || estancia == null || valor == null || cargoMascotas == null) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "Faltan datos para la cotización.");
        }
        if (ocupantesTotales < 1 || ocupantesFacturables < 0 || ocupantesFacturables > ocupantesTotales) {
            throw new ReglaDominioException("PARAMETRO_INVALIDO", "La cotización tiene un número de ocupantes inválido.");
        }
    }

    /** Alojamiento (redondeado al final, 3.4) más el cargo por mascotas (RP-02). */
    public Dinero total() {
        return valor.total().sumar(cargoMascotas).redondear();
    }
}

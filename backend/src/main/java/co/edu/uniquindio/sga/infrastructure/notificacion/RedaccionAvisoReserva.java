package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.repository.NotificadorTitular.Motivo;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Asunto y cuerpo del aviso al titular. Texto plano: lo lee igual un buzón de pruebas que un correo real. */
final class RedaccionAvisoReserva {

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES_CO);

    private RedaccionAvisoReserva() {
    }

    static String asunto(String alojamiento, Reserva reserva, Motivo motivo) {
        String accion = switch (motivo) {
            case CREADA -> "recibimos su reserva";
            case CONFIRMADA -> "su reserva está confirmada";
            case CANCELADA -> "su reserva fue cancelada";
        };
        return alojamiento + ": " + accion + " " + reserva.codigo().valor();
    }

    static String cuerpo(String alojamiento, Reserva reserva, Motivo motivo) {
        NumberFormat pesos = NumberFormat.getCurrencyInstance(ES_CO);
        pesos.setMaximumFractionDigits(0);
        String estancia = "Apartamento " + reserva.apartamento().valor() + ", del "
                + FECHA.format(reserva.estancia().entrada()) + " al " + FECHA.format(reserva.estancia().salida())
                + " (" + reserva.estancia().noches() + (reserva.estancia().noches() == 1 ? " noche" : " noches") + ").";
        String detalle = switch (motivo) {
            case CREADA -> "Su reserva quedó registrada por " + pesos.format(reserva.valor().total().valor())
                    + ". Queda pendiente hasta confirmarse.";
            case CONFIRMADA -> "Lo esperamos. Puede indicar o cambiar su hora de llegada desde la aplicación.";
            case CANCELADA -> "Las noches quedaron libres. El folio de la reserva muestra lo retenido según la "
                    + "política de cancelación de su reserva.";
        };
        return "Hola, " + reserva.titular().nombre() + ".\n\n" + estancia + "\n" + detalle + "\n\n"
                + "Código de reserva: " + reserva.codigo().valor() + "\n" + alojamiento;
    }
}

package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.model.folio.MedioPago;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Valores de la Ficha del Alojamiento (Anexo A) leídos de {@code application.properties}, prefijo
 * {@code sga.alojamiento}. Las horas se escriben "HH:mm" y el dinero en pesos COP sin decimales.
 */
@ConfigurationProperties(prefix = "sga.alojamiento")
public record PropiedadesAlojamiento(
        String nombre,
        String descripcion,
        String ciudad,
        String direccion,
        double latitud,
        double longitud,
        List<String> normasConvivencia,
        List<ServicioProps> servicios,
        List<MedioPago> mediosPago,
        int umbralEdadFacturable,
        String horaEntrada,
        String horaSalida,
        int tiempoPreparacionHoras,
        int plazoConfirmacionHoras,
        String horaLimiteNoShow,
        AnticipoProps anticipo,
        MascotasProps mascotas,
        RecargoNocturnoProps recargoLlegadaNocturna) {

    public record ServicioProps(String nombre, boolean generaCargo, long valor) {
    }

    public record AnticipoProps(boolean exigido, int porcentaje) {
    }

    public record MascotasProps(int cupoMaximo, long cargoPorNoche) {
    }

    public record RecargoNocturnoProps(String horaInicio, long valor) {
    }
}

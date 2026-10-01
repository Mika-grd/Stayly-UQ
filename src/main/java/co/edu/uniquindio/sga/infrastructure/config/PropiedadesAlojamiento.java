package co.edu.uniquindio.sga.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores de la Ficha del Alojamiento (Anexo A) leídos de {@code application.yml}, prefijo
 * {@code sga.alojamiento}. Las horas se escriben "HH:mm" y el dinero en pesos COP sin decimales.
 */
@ConfigurationProperties(prefix = "sga.alojamiento")
public record PropiedadesAlojamiento(
        int umbralEdadFacturable,
        String horaEntrada,
        String horaSalida,
        int tiempoPreparacionHoras,
        int plazoConfirmacionHoras,
        String horaLimiteNoShow,
        AnticipoProps anticipo,
        MascotasProps mascotas,
        RecargoNocturnoProps recargoLlegadaNocturna) {

    public record AnticipoProps(boolean exigido, int porcentaje) {
    }

    public record MascotasProps(int cupoMaximo, long cargoPorNoche) {
    }

    public record RecargoNocturnoProps(String horaInicio, long valor) {
    }
}

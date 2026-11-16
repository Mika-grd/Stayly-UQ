package co.edu.uniquindio.sga.infrastructure.programacion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Activa las tareas programadas (vencimiento de reservas pendientes, RN-21). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "sga.vencimiento.habilitado", havingValue = "true", matchIfMissing = true)
public class ConfiguracionProgramacion {
}

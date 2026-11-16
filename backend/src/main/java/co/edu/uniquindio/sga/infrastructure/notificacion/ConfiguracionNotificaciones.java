package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.repository.NotificadorTitular;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/** Hilos de los avisos y adaptador nulo cuando la integración está apagada (I-02). */
@Configuration
public class ConfiguracionNotificaciones {

    /**
     * Dos hilos y una cola acotada: los avisos nunca compiten con las peticiones. Si la cola se
     * llena el aviso se descarta y queda en el registro; la operación de negocio ya terminó.
     */
    @Bean(name = "ejecutorNotificaciones")
    public Executor ejecutorNotificaciones() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(1);
        ejecutor.setMaxPoolSize(2);
        ejecutor.setQueueCapacity(100);
        ejecutor.setThreadNamePrefix("aviso-");
        ejecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        ejecutor.initialize();
        return ejecutor;
    }

    /** Con {@code sga.notificaciones.habilitadas=false} los casos de uso siguen igual, sin avisar. */
    @Bean
    @ConditionalOnMissingBean(NotificadorTitular.class)
    public NotificadorTitular notificadorTitularApagado() {
        return NotificadorTitular.NINGUNO;
    }
}

package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.infrastructure.persistence.datos.CargaDatosPrueba;
import co.edu.uniquindio.sga.infrastructure.security.CredencialesCanalServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Credencial conocida para el canal del simulador del curso (12.3), así la colección .http y las
 * pruebas pueden llamar a /api/externo/** sin registrar un canal antes. Es un dato de prueba de H2,
 * no una credencial real; sin la propiedad no se carga.
 */
@Component
@ConditionalOnProperty(name = "sga.canal-simulador.credencial")
public class CredencialCanalSimuladorInitializer implements CommandLineRunner {

    private final CredencialesCanalServiceImpl credenciales;
    private final String credencial;

    public CredencialCanalSimuladorInitializer(CredencialesCanalServiceImpl credenciales,
                                               @Value("${sga.canal-simulador.credencial}") String credencial) {
        this.credenciales = credenciales;
        this.credencial = credencial;
    }

    @Override
    public void run(String... args) {
        if (!credenciales.estaActiva(CargaDatosPrueba.CANAL_SIMULADOR)) {
            credenciales.registrar(CargaDatosPrueba.CANAL_SIMULADOR, credencial);
        }
    }
}

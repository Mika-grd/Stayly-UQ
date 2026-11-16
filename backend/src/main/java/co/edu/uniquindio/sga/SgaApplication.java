package co.edu.uniquindio.sga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SgaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgaApplication.class, args);
    }
}

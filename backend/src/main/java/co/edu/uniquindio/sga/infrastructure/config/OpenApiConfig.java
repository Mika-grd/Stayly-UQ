package co.edu.uniquindio.sga.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Esquemas de seguridad de Swagger UI (Guía 12, 6.5): los mismos bearerAuth y apiKey de
 * docs/api/openapi.yaml. Hacen aparecer el botón Authorize para pegar el token o la credencial del canal.
 */
@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "apiKey", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-API-Key",
        description = "Credencial del canal externo, solo para /api/externo/**.")
public class OpenApiConfig {
}

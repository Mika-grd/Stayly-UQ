package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadena de filtros de seguridad del SGA (Guía 12, 5.4). Por URL solo se separa lo público de lo
 * protegido; la matriz de roles (7.1) se aplica por método con {@code @PreAuthorize}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Rutas sin token: login, registro, la página de error y Swagger (herramienta de desarrollo). */
    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login",
            "/api/auth/registro",
            "/error",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /** Solo GET: buscar apartamentos disponibles, ver el detalle de uno y la política vigente. */
    private static final String[] LECTURAS_PUBLICAS = {
            "/api/apartamentos/disponibles",
            "/api/apartamentos/*",
            "/api/politica-cancelacion/vigente"
    };

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final RespuestasErrorSeguridad respuestasError;

    public SecurityConfig(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter,
                          RespuestasErrorSeguridad respuestasError) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.respuestasError = respuestasError;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Sin cookies de sesión no hay CSRF: el token viaja en un encabezado (5.4)
                .csrf(AbstractHttpConfigurer::disable)
                // La consola de H2 se dibuja en un iframe del mismo origen
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        // Lecturas públicas del contrato (security: []): catálogo y política vigente
                        .requestMatchers(HttpMethod.GET, LECTURAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(respuestasError)
                        .accessDeniedHandler(respuestasError))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestasError)
                        .accessDeniedHandler(respuestasError))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    /** BCrypt con el algoritmo guardado junto al hash ({bcrypt}$2a$10$...). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

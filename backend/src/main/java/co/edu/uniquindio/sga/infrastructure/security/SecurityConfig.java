package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Cadena de filtros de seguridad del SGA (Guía 12, 5.4). Por URL solo se separa lo público de lo
 * protegido; la matriz de roles (7.1) se aplica por método con {@code @PreAuthorize}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Rutas sin token: login, registro, recuperación, la página de error y el health check de la plataforma. */
    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login",
            "/api/auth/registro",
            "/api/auth/recuperacion",
            "/api/auth/recuperacion/confirmar",
            "/error",
            "/actuator/health"
    };

    /** Documentación y consola de H2: libres en desarrollo, cerradas en producción (Guía 22, 4.5). */
    private static final String[] HERRAMIENTAS_DESARROLLO = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /** Solo GET: buscar apartamentos disponibles, ver el detalle de uno y la política vigente. */
    private static final String[] LECTURAS_PUBLICAS = {
            "/api/apartamentos/disponibles",
            "/api/apartamentos/*",
            "/api/apartamentos/*/disponibilidad",
            "/api/politica-cancelacion/vigente"
    };

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final RespuestasErrorSeguridad respuestasError;
    private final CredencialesCanalServiceImpl credencialesCanal;
    private final boolean herramientasDesarrollo;

    public SecurityConfig(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter,
                          RespuestasErrorSeguridad respuestasError, CredencialesCanalServiceImpl credencialesCanal,
                          @Value("${sga.seguridad.herramientas-desarrollo:true}") boolean herramientasDesarrollo) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.respuestasError = respuestasError;
        this.credencialesCanal = credencialesCanal;
        this.herramientasDesarrollo = herramientasDesarrollo;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UrlBasedCorsConfigurationSource corsConfigurationSource)
            throws Exception {
        return http
                // Sin cookies de sesión no hay CSRF: el token viaja en un encabezado (5.4)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS dentro de la cadena de seguridad, con los orígenes de la configuración (Guía 22, 4.3)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // La consola de H2 se dibuja en un iframe del mismo origen
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> {
                    if (herramientasDesarrollo) {
                        auth.requestMatchers(PathRequest.toH2Console()).permitAll()
                                .requestMatchers(HERRAMIENTAS_DESARROLLO).permitAll();
                    }
                    auth
                            .requestMatchers(RUTAS_PUBLICAS).permitAll()
                            // Ruta literal que la lectura pública /api/apartamentos/* también cubriría
                            .requestMatchers(HttpMethod.GET, "/api/apartamentos/por-preparar").authenticated()
                            // Lecturas públicas del contrato (security: []): catálogo y política vigente
                            .requestMatchers(HttpMethod.GET, LECTURAS_PUBLICAS).permitAll()
                            // Canal externo: credencial X-API-Key en vez de JWT (7.8)
                            .requestMatchers(FiltroCredencialCanal.RUTAS + "**").hasAuthority(CanalExternoAutenticado.AUTORIDAD)
                            .anyRequest().authenticated();
                })
                .addFilterBefore(new FiltroCredencialCanal(credencialesCanal, respuestasError),
                        BearerTokenAuthenticationFilter.class)
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

    /**
     * Orígenes del frontend autorizados (Guía 22, 4.3): en local el servidor de Vite; en producción la
     * URL de Vercel, por la variable SGA_CORS_ALLOWED_ORIGINS. Solo los métodos del API; DELETE es la
     * eliminación lógica de apartamentos y usuarios (7.2, 7.3), nunca un borrado físico.
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${sga.cors.allowed-origins:http://localhost:5173,http://localhost:5174}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
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

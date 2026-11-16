# SGA · Stayly-UQ — Entrega 2

Sistema de Gestión de Alojamiento para Stayly-UQ (apartahotel de 6 apartamentos en Armenia, Quindío).
Programación Avanzada · Universidad del Quindío · 2026-2.

Esta entrega contiene el backend: el **dominio** de la Entrega 1 (arquitectura hexagonal, sin Spring ni JPA),
sus **casos de uso** con Spring Boot, la **API REST** con DTOs, MapStruct y Bean Validation, la **persistencia en
H2** con adaptadores JPA y datos de prueba que se cargan solos, las **consultas paginadas** de a 10 y la
**seguridad con JWT** (BCrypt, roles de 7.1 y verificación por propietario).

Todas las operaciones de `docs/api/openapi.yaml` están implementadas: reservas y operación diaria, el **folio**
(cargos, pagos y cierre, 7.7), el **canal externo** con su credencial `X-API-Key` y la revisión de **conflictos**
(7.8) y los **reportes** del administrador (ocupación, ingresos y reservas por canal).

## Requisitos y clave del JWT

Un JDK 17 o superior para ejecutar Gradle. El proyecto compila con Java 21: si no está instalado, Gradle lo
descarga solo (toolchain).

La clave del JWT no se versiona. Antes de arrancar, copie
`backend/src/main/resources/application-secrets.properties.example` como `application-secrets.properties` en la
misma carpeta y ponga una clave aleatoria de al menos 32 caracteres (por ejemplo, `openssl rand -base64 48`).

## Ejecutar

```bash
cd backend
./gradlew clean test     # pruebas unitarias, de persistencia, de seguridad y la colección completa de la API
./gradlew bootRun        # API en http://localhost:8080
```

- Swagger UI: <http://localhost:8080/swagger-ui.html> (botón **Authorize** con el token de `POST /api/auth/login`,
  o con la credencial del canal para `/api/externo/**`).
- Consola de H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:sga`, usuario `sa`, sin contraseña).
- Colección de pruebas: `docs/api/coleccion-sga.http` (instrucciones en `docs/api/LEEME.md`).

Usuarios iniciales (se cargan solos al arrancar, con la contraseña en BCrypt):

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@stayly.local | Admin2026 |
| Recepcionista | recepcion@stayly.local | Recepcion2026 |
| Personal de servicio | servicio@stayly.local | Servicio2026 |
| Huésped (Laura Gómez) | huesped1@stayly.local | Huesped2026 |
| Huésped (Carlos Mejía) | huesped2@stayly.local | Huesped2026 |

El canal externo no usa JWT sino su credencial en el encabezado `X-API-Key`. El administrador la obtiene al
registrar el canal con `POST /api/canales` (se muestra una sola vez; solo se guarda su huella SHA-256). Para probar
sin registrar nada, el canal del simulador de los datos de prueba usa la credencial `sim-canal-uq-2026`
(`sga.canal-simulador.credencial` en `application.properties`; dato de prueba de H2, no una credencial real).

## Estructura

```
backend/      API en Spring Boot (Gradle)
frontend/     aplicación web (pendiente)
docs/         documentación compartida por ambos
```

```
backend/src/main/java/co/edu/uniquindio/sga/
  domain/                 agregados, objetos de valor, servicios de dominio y puertos (repositorios y
                          ConfiguracionAlojamiento). Solo depende de java.*
  application/
    usecase/              casos de uso (@Service, @Transactional) que orquestan el dominio
    dto/request|response  DTOs (record) del contrato
    exception/            excepciones de aplicación (recurso inexistente, acceso)
  infrastructure/
    web/                  controllers delgados, mappers MapStruct y @RestControllerAdvice
    security/             cadena de filtros, JWT, credencial X-API-Key del canal y verificación por propietario
    persistence/jpa/      entidades, @Embeddable, mappers y adaptadores de cada repositorio
    persistence/datos/    datos de prueba de la sección 12.3
    config/               beans del dominio y valores de la Ficha leídos de application.properties
docs/
  modelo/     libro del modelo (Excel): identificación, invariantes, servicios, casos de uso, matriz, errores
  diagramas/  diagrama de clases (Mermaid) y mapa de agregados
  anexos/     Ficha del alojamiento y preguntas guía
  api/        openapi.yaml y la colección de pruebas
```

Los valores de la Ficha del Alojamiento (umbral de edad, horarios, anticipo, mascotas, recargo nocturno…) están
en `backend/src/main/resources/application.properties`: se cambian sin recompilar, y el administrador también los
cambia en caliente con `PUT /api/alojamiento` y `PUT /api/alojamiento/parametros`.

Errores: toda respuesta de error es un `ErrorNegocioResponse`. 400 formato del mensaje, 401 y 403 seguridad,
404 recurso inexistente y 409 regla del negocio violada.

## Validar el contrato

```bash
npx @redocly/cli lint docs/api/openapi.yaml
```

El contrato es válido. Quedan tres advertencias de estilo que se aceptan: la licencia no tiene URL, el servidor es
`localhost` (entorno de desarrollo) y `GET /api/politica-cancelacion/vigente` no documenta un 4XX porque es una
consulta pública sin parámetros. Además, `ContratoOpenApiTest` comprueba en cada `./gradlew test` que el YAML y los
controllers tengan las mismas operaciones.

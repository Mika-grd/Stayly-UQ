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
También puede pasarla en la variable `SGA_JWT_SECRET`, que tiene prioridad. Ese archivo nunca entra al jar.

## Ejecutar

```bash
cd backend
./gradlew clean test     # pruebas unitarias, de persistencia, de seguridad y la colección completa de la API
./gradlew bootRun        # API en http://localhost:8080
```

- Interfaz web: con el backend encendido, `cd frontend && npm install && npm run dev` y abra
  <http://localhost:5173> (detalles en `frontend/README.md`).
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
frontend/     aplicación web en React (Vite + TypeScript)
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

## Integraciones (sección 10)

Cada integración está detrás de un puerto (I-01), tiene una alternativa local que funciona sin internet ni
credenciales y que usan las pruebas (I-02, I-08), un tiempo máximo (I-03) y un comportamiento ante fallo que nunca
impide reservar, registrar, cobrar ni cerrar un folio (I-04, I-05). Todo se cambia por configuración.

| Integración | Puerto | Por defecto (local) | Real | Ante fallo |
|---|---|---|---|---|
| Avisos al titular al crear, confirmar y cancelar (10.3.2, opción A) | `NotificadorTitular`, `NotificadorCorreo` | Buzón simulado: el correo queda en el log | SMTP: `SGA_CORREO_PROVEEDOR=smtp` y `SGA_SMTP_HOST`, `SGA_SMTP_PUERTO`, `SGA_SMTP_USUARIO`, `SGA_SMTP_CLAVE`, `SGA_CORREO_REMITENTE` (Gmail con clave de aplicación, Brevo o Mailtrap, todos gratuitos) | Se envía después del commit y en otro hilo, con 5 s máximo; un fallo queda en el log y la reserva sigue. `sga.notificaciones.habilitadas=false` los apaga |
| Modelo de IA: resumen del día en recepción (10.2) | `AsistenteIA` | Apagado (`SGA_IA_PROVEEDOR=ninguno`): recepción ve "Resumen automático no disponible" y sigue con sus listas | API compatible con OpenAI: `SGA_IA_PROVEEDOR=openai-compatible`, `SGA_IA_URL`, `SGA_IA_MODELO`, `SGA_IA_CLAVE` (Groq o Gemini con nivel gratuito, u Ollama local sin clave) | 8 s máximo; error o demora = resumen no disponible, nunca un error. Al modelo no van nombres, documentos ni contactos |
| Canal externo (10.3.1) | `CredencialesCanalService` | Canal del simulador con credencial de prueba | Canales registrados por el administrador | Conflictos e idempotencia en el dominio (RN-18, RN-19) |
| Mapa del alojamiento | (frontend) | Teselas de OpenStreetMap, sin credenciales | Mapbox con `VITE_MAPBOX_TOKEN` | Sin mapa, el resto de la página funciona |

Ejemplos para encender la IA: Ollama local `SGA_IA_PROVEEDOR=openai-compatible SGA_IA_URL=http://localhost:11434/v1
SGA_IA_MODELO=llama3.2`; Groq `SGA_IA_URL=https://api.groq.com/openai/v1 SGA_IA_MODELO=llama-3.1-8b-instant
SGA_IA_CLAVE=...`. Para la sustentación con la IA deshabilitada basta `SGA_IA_PROVEEDOR=ninguno`.

## Despliegue

El backend va a Render como contenedor (`backend/Dockerfile`) y el frontend a Vercel, según la Guía 22.

| Parte | URL |
|---|---|
| Aplicación | _pendiente: URL de Vercel_ |
| API | _pendiente: URL de Render_ |
| Estado del API | _pendiente: URL de Render_ + `/actuator/health` |

> El API usa el plan gratuito de Render: la primera petición después de 15 minutos sin uso tarda cerca de un
> minuto, y los datos vuelven al estado inicial en cada reinicio.

Configuración versionada: `backend/Dockerfile`, `backend/.dockerignore`, `backend/.gitattributes`,
`backend/src/main/resources/application-prod.properties`, `frontend/vercel.json` y `frontend/.env.example`.

**Render** (Web Service, Docker, Root Directory `backend`, Health Check Path `/actuator/health`):

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SGA_JWT_SECRET` | `openssl rand -hex 32`, distinta a la local |
| `SGA_CORS_ALLOWED_ORIGINS` | URL exacta de Vercel, sin `/` final |
| `SGA_USUARIOS_INICIALES_HABILITADOS` | `true` (la base se borra al dormir el servicio) |
| `SGA_ADMIN_CORREO`, `SGA_ADMIN_CONTRASENA`, `SGA_ADMIN_TELEFONO`, `SGA_ADMIN_DOCUMENTO`, `SGA_ADMIN_FECHA_NACIMIENTO` | Cuenta del administrador; la contraseña no va en el repositorio |

Opcionales en Render: las variables de correo y de IA de la sección **Integraciones** (sin ellas, avisos al log e
IA apagada).

**Vercel** (Root Directory `frontend`, preset Vite): `VITE_API_URL` con la URL de Render terminada en `/api`, y
opcionalmente `VITE_MAPBOX_TOKEN`.

En el perfil `prod` la consola de H2 y Swagger quedan cerrados, solo existe la cuenta del administrador (las de la
tabla anterior no se crean) y el arranque falla si falta la clave o un dato de esa cuenta. Los datos de prueba del
alojamiento (apartamentos, tarifas y reservas) sí se cargan, para que el catálogo funcione en la demostración.

## Validar el contrato

```bash
npx @redocly/cli lint docs/api/openapi.yaml
```

El contrato es válido. Quedan tres advertencias de estilo que se aceptan: la licencia no tiene URL, el servidor es
`localhost` (entorno de desarrollo) y `GET /api/politica-cancelacion/vigente` no documenta un 4XX porque es una
consulta pública sin parámetros. Además, `ContratoOpenApiTest` comprueba en cada `./gradlew test` que el YAML y los
controllers tengan las mismas operaciones.

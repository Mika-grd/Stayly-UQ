# Contrato y colección de la API

- `openapi.yaml`: contrato de la API en OpenAPI 3.1, generado a partir de las hojas APIs, Schemas DTO y
  Errores de Negocio del libro del modelo, y ampliado con las operaciones de los cortes 3 y 4 (inventario,
  calendario de disponibilidad, resumen del día con IA). Todas están implementadas. Con la aplicación arriba, Swagger UI está en
  <http://localhost:8080/swagger-ui.html> (botón **Authorize** con el token del login).
- `coleccion-sga.http`: colección de pruebas para el IntelliJ HTTP Client. Incluye el flujo completo de
  una reserva, un 409 por cada regla que expone la API, los casos 400, 401, 403 y 404, los casos de
  seguridad de la sección 9 de la Guía 12, el folio (11), el canal externo con conflicto e idempotencia
  (12), los reportes (13) y el inventario, el calendario del detalle y la IA apagada (14).
- `../trazabilidad.md`: matriz de trazabilidad, de cada regla al código que la protege, el endpoint que la
  expone con su código de error y la prueba que la verifica.

## Ejecutar la colección

1. Arranque la aplicación: `cd backend && ./gradlew bootRun`.
2. Abra `coleccion-sga.http` en IntelliJ y use **Run All Requests in File**, o desde la consola:
   `ijhttp docs/api/coleccion-sga.http`.

No hay que preparar nada a mano: cada petición guarda los tokens, las fechas (relativas a hoy) y los
códigos de reserva que usan las siguientes. Para repetirla, reinicie la aplicación y H2 vuelve a los
datos de prueba. La prueba `ColeccionHttpIntegracionTest` ejecuta este mismo archivo con
`./gradlew test`, así que si una respuesta cambia, la compilación lo detecta.

El caso de token vencido no está en la ejecución automática: emita un token, espere los 30 minutos
de `sga.jwt.expiracion-minutos` y repita cualquier petición protegida (responde 401).

## Validar el contrato

```bash
npx @redocly/cli lint docs/api/openapi.yaml
```

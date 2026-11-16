# Frontend · Stayly-UQ

Aplicación web del SGA en **React 18 + TypeScript**, con Vite y React Router. Consume la API del `backend/`
según el contrato de [`docs/api/openapi.yaml`](../docs/api/openapi.yaml). Ninguna regla del negocio vive
aquí: la interfaz valida formatos para ayudar a quien escribe, y muestra el mensaje de negocio que responde
el dominio (`ErrorNegocioResponse`).

## Ejecutar

Con el backend encendido (`cd backend && ./gradlew bootRun`, puerto 8080):

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173; /api se reenvía al backend (sin CORS)
npm run build      # verifica los tipos y genera dist/
```

Usuarios de prueba: los de la tabla del README principal (por ejemplo `huesped1@stayly.local` / `Huesped2026`).

## Qué cubre (corte 3)

| Pantalla | Ruta | Quién | Casos de uso |
|---|---|---|---|
| Búsqueda de apartamentos libres | `/` | Todos | CU-20 |
| Detalle, disponibilidad, precio noche por noche y política | `/apartamentos/:id` | Todos (desglose con sesión) | CU-01 |
| Reservar | `/reservar/:id` | Huésped (portal) y recepción (directa) | CU-02 |
| Mis viajes | `/viajes` | Huésped | 7.2 |
| Reserva: recorrido, hora de llegada, confirmar, llegada, salida, no-show y cancelación | `/reservas/:codigo` | Titular y recepción | CU-03, 04, 05, 06, 12, 14 |
| Folio: cargos, pagos, saldo y cierre | dentro de la reserva | Titular (ver) y recepción | CU-07, 18, 19 |
| Recepción: llegadas y salidas del día, listado con filtros | `/recepcion` | Recepción y administración | CU-42 |
| Operación: por preparar, estado operativo y calendario de ocupación | `/operacion` | Servicio, recepción y administración | CU-09, 10, 15, 21, 35, 43 |
| Entrar y crear cuenta | `/entrar`, `/registro` | Todos | 7.2 |

## Estructura

```
src/
  api/          cliente HTTP (token, errores de negocio), tipos del contrato y servicios por recurso
  auth/         sesión por rol y guarda de rutas
  components/   fachada ilustrada, cabecera, búsqueda, tarjeta, folio y piezas comunes
  pages/        una página por ruta
  lib/          formato (dinero, fechas, nombres de estados), colores y carga de datos
  styles/       tokens, base, componentes y páginas
```

El diseño está explicado en [`DISENO.md`](DISENO.md).

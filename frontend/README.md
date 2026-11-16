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

El mapa del alojamiento usa Mapbox si existe `VITE_MAPBOX_TOKEN` (token público, en `.env.local`); sin él,
arma el mapa con teselas de OpenStreetMap, que no piden credenciales. El código de recuperación de
contraseña llega al buzón simulado del backend (el log de `NotificadorCorreoRegistro`).

## Qué cubre

| Pantalla | Ruta | Quién | Casos de uso |
|---|---|---|---|
| Búsqueda de apartamentos libres, filtros por capacidad, precio y características, mapa | `/` | Todos | CU-20 |
| Detalle: galería, calendario de disponibilidad, mapa, desglose noche por noche y política | `/apartamentos/:id` | Todos (desglose con sesión) | CU-01, 7.1 |
| Reservar, con desglose noche por noche | `/reservar/:id` | Huésped (portal) y recepción (directa) | CU-02 |
| Modificar fechas, ocupantes o apartamento, con el ajuste que irá al folio | `/reservas/:codigo/modificar` | Recepción y administración | CU-13 |
| Mis viajes | `/viajes` | Huésped | 7.2 |
| Reserva: recorrido, hora de llegada, confirmar, llegada, salida, no-show y cancelación | `/reservas/:codigo` | Titular y recepción | CU-03, 04, 05, 06, 12, 14 |
| Folio: cargos, pagos, saldo y cierre | dentro de la reserva | Titular (ver) y recepción | CU-07, 18, 19 |
| Recepción: resumen del día redactado por IA (si está encendida), llegadas y salidas del día, listado con filtros por estado, canal, apartamento, fechas y titular | `/recepcion` | Recepción y administración | CU-42 |
| Operación: por preparar, estado operativo, calendario de ocupación y novedades | `/operacion` | Servicio, recepción y administración | CU-09, 10, 11, 15, 21, 35, 41, 43 |
| Perfil: nombre, teléfono, foto y cambio de contraseña | `/perfil` | Todos con sesión | CU-39 |
| Administración: inventario (crear, editar, activar, retirar), tarifas con histórico, bloqueos, temporadas, versiones de la política, datos y parámetros del alojamiento, reglas propias, usuarios y roles, canales con su credencial, conflictos, bitácora y reportes con descarga en hoja de cálculo | `/admin/:seccion` | Administración | 7.1 Administrador, 7.3, 7.4, 7.8, 7.9, L-23 |
| Entrar, crear cuenta y recuperar contraseña con código | `/entrar`, `/registro`, `/recuperar` | Todos | 7.2, CU-40 |

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

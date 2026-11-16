import type { ErrorNegocio } from './tipos';

const SESION = 'stayly.sesion';

export interface Sesion {
  token: string;
  rol: string;
  expira: number; // epoch ms
}

export function leerSesion(): Sesion | null {
  try {
    const crudo = localStorage.getItem(SESION);
    if (!crudo) return null;
    const sesion = JSON.parse(crudo) as Sesion;
    return sesion.expira > Date.now() ? sesion : null;
  } catch {
    return null;
  }
}

export function guardarSesion(sesion: Sesion | null) {
  try {
    if (sesion) localStorage.setItem(SESION, JSON.stringify(sesion));
    else localStorage.removeItem(SESION);
  } catch {
    // Sin almacenamiento (modo privado): la sesión dura lo que dure la pestaña.
  }
}

/** Error de la API con el código de negocio del contrato (ErrorNegocioResponse). */
export class ErrorApi extends Error {
  readonly codigo: string;
  readonly estado: number;
  readonly regla?: string | null;

  constructor(estado: number, cuerpo: Partial<ErrorNegocio>) {
    super(cuerpo.mensaje ?? 'No se pudo completar la operación.');
    this.codigo = cuerpo.codigo ?? 'ERROR';
    this.estado = estado;
    this.regla = cuerpo.regla;
  }
}

let alExpirar: () => void = () => {};

/** La sesión vencida o inválida (401) cierra la sesión en toda la aplicación. */
export function alExpirarSesion(accion: () => void) {
  alExpirar = accion;
}

type Metodo = 'GET' | 'POST' | 'PUT' | 'DELETE';

/**
 * URL base del API (Guía 22, 6.1). En producción VITE_API_URL trae la URL pública del backend con su
 * /api; sin ella se usa /api en el mismo origen, que en desarrollo el proxy de Vite lleva al 8080.
 */
const BASE_API = (import.meta.env.VITE_API_URL?.trim() || '/api').replace(/\/+$/, '');
const urlDe = (ruta: string) => BASE_API + ruta.replace(/^\/api/, '');

/** 70 s: cubren el arranque en frío del plan gratuito, que tarda cerca de un minuto en despertar. */
const TIEMPO_LIMITE_MS = 70_000;

async function pedir<T>(metodo: Metodo, ruta: string, cuerpo?: unknown): Promise<T> {
  const sesion = leerSesion();
  const encabezados: Record<string, string> = { Accept: 'application/json' };
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json';
  if (sesion) encabezados.Authorization = `Bearer ${sesion.token}`;

  let respuesta: Response;
  const control = new AbortController();
  const limite = setTimeout(() => control.abort(), TIEMPO_LIMITE_MS);
  try {
    respuesta = await fetch(urlDe(ruta), {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
      signal: control.signal,
    });
  } catch {
    throw new ErrorApi(0, {
      codigo: 'SIN_CONEXION',
      mensaje: control.signal.aborted
        ? 'El servidor tardó demasiado en responder. Intente de nuevo en un momento.'
        : 'No hay conexión con el servidor.',
    });
  } finally {
    clearTimeout(limite);
  }

  const texto = await respuesta.text();
  const datos = texto ? JSON.parse(texto) : undefined;
  if (!respuesta.ok) {
    if (respuesta.status === 401 && sesion) alExpirar();
    throw new ErrorApi(respuesta.status, datos ?? {});
  }
  return datos as T;
}

export const api = {
  get: <T>(ruta: string) => pedir<T>('GET', ruta),
  post: <T>(ruta: string, cuerpo?: unknown) => pedir<T>('POST', ruta, cuerpo ?? {}),
  put: <T>(ruta: string, cuerpo?: unknown) => pedir<T>('PUT', ruta, cuerpo),
};

/** Arma una consulta ?a=1&b=2, repitiendo la clave en las listas y omitiendo los vacíos. */
export function consulta(parametros: Record<string, string | number | undefined | null | (string | number)[]>) {
  const q = new URLSearchParams();
  Object.entries(parametros).forEach(([clave, valor]) => {
    if (valor === undefined || valor === null || valor === '') return;
    if (Array.isArray(valor)) valor.forEach((v) => q.append(clave, String(v)));
    else q.append(clave, String(valor));
  });
  const texto = q.toString();
  return texto ? `?${texto}` : '';
}

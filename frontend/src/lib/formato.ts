import type { CanalOrigen, EstadoOperativo, EstadoReserva, MedioPago, Rol, TipoCargo } from '../api/tipos';

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

export const dinero = (valor: number | null | undefined) => (valor == null ? '—' : pesos.format(valor));

/** yyyy-MM-dd a Date local, sin correr el día por la zona horaria. */
export function aFecha(iso: string): Date {
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(a, m - 1, d);
}

export function iso(fecha: Date): string {
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${fecha.getFullYear()}-${dos(fecha.getMonth() + 1)}-${dos(fecha.getDate())}`;
}

export const hoy = () => iso(new Date());

export function sumarDias(fechaIso: string, dias: number): string {
  const f = aFecha(fechaIso);
  f.setDate(f.getDate() + dias);
  return iso(f);
}

export function restarAnios(fechaIso: string, anios: number): string {
  const f = aFecha(fechaIso);
  f.setFullYear(f.getFullYear() - anios);
  return iso(f);
}

export const noches = (entrada: string, salida: string) =>
  Math.round((aFecha(salida).getTime() - aFecha(entrada).getTime()) / 86_400_000);

const corta = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' });
const larga = new Intl.DateTimeFormat('es-CO', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
const conHora = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' });

export const fechaCorta = (v: string) => corta.format(aFecha(v));
export const fechaLarga = (v: string) => larga.format(aFecha(v));
export const fechaHora = (v: string) => conHora.format(new Date(v));
export const rango = (entrada: string, salida: string) => `${fechaCorta(entrada)} al ${fechaCorta(salida)}`;
export const hora = (v: string | null) => (v ? v.slice(0, 5) : '—');

export const nombreEstado: Record<EstadoReserva, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  NO_SHOW: 'No llegó',
};

export const nombreOperativo: Record<EstadoOperativo, string> = {
  PREPARADO: 'Preparado',
  OCUPADO: 'Ocupado',
  PENDIENTE_PREPARACION: 'Por preparar',
  EN_PREPARACION: 'En preparación',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
};

export const nombreCanal: Record<CanalOrigen, string> = {
  PORTAL: 'Portal',
  DIRECTO: 'Recepción',
  EXTERNO: 'Canal externo',
};

export const nombreMedio: Record<MedioPago, string> = {
  EFECTIVO: 'Efectivo',
  TRANSFERENCIA: 'Transferencia',
  TARJETA: 'Tarjeta',
};

export const nombreCargo: Record<TipoCargo, string> = {
  ALOJAMIENTO: 'Alojamiento',
  SERVICIO_ADICIONAL: 'Servicio adicional',
  AJUSTE: 'Ajuste',
  PENALIDAD: 'Penalidad',
};

export const nombreRol: Record<Rol, string> = {
  HUESPED: 'Huésped',
  RECEPCIONISTA: 'Recepción',
  ADMINISTRADOR: 'Administración',
  PERSONAL_SERVICIO: 'Servicio',
};

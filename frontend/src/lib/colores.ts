import type { EstadoOperativo, EstadoReserva } from '../api/tipos';

export type ColorFachada = 'balcon' | 'cobalto' | 'cafetal' | 'maiz' | 'lila' | 'aguamarina';

const ORDEN: ColorFachada[] = ['balcon', 'cobalto', 'cafetal', 'maiz', 'lila', 'aguamarina'];

/** Los seis de la Ficha tienen color propio y distinto; uno nuevo toma el suyo de su identificación. */
const FICHA: Record<string, ColorFachada> = {
  'SUQ-101': 'balcon',
  'SUQ-102': 'cobalto',
  'SUQ-201': 'cafetal',
  'SUQ-202': 'maiz',
  'SUQ-301': 'lila',
  'SUQ-302': 'aguamarina',
};

/** Cada apartamento tiene siempre el mismo color de fachada. */
export function colorDe(identificacion: string): ColorFachada {
  if (FICHA[identificacion]) return FICHA[identificacion];
  let h = 0;
  for (const c of identificacion) h = (h * 31 + c.charCodeAt(0)) >>> 0;
  return ORDEN[h % ORDEN.length];
}

export const variable = (color: ColorFachada) => `var(--${color})`;
export const textoSobre = (color: ColorFachada) => `var(--sobre-${color})`;

const ESTADO_RESERVA: Record<EstadoReserva, ColorFachada | 'muro'> = {
  PENDIENTE: 'maiz',
  CONFIRMADA: 'cobalto',
  EN_CURSO: 'cafetal',
  FINALIZADA: 'muro',
  CANCELADA: 'balcon',
  NO_SHOW: 'lila',
};

const ESTADO_OPERATIVO: Record<EstadoOperativo, ColorFachada | 'muro'> = {
  PREPARADO: 'cafetal',
  OCUPADO: 'cobalto',
  PENDIENTE_PREPARACION: 'maiz',
  EN_PREPARACION: 'aguamarina',
  FUERA_DE_SERVICIO: 'balcon',
};

function estilo(color: ColorFachada | 'muro') {
  return color === 'muro'
    ? { '--chip': 'var(--muro)', '--chip-texto': 'var(--tinta)' }
    : { '--chip': variable(color), '--chip-texto': textoSobre(color) };
}

export const estiloEstadoReserva = (estado: EstadoReserva) => estilo(ESTADO_RESERVA[estado]) as React.CSSProperties;
export const estiloEstadoOperativo = (estado: EstadoOperativo) =>
  estilo(ESTADO_OPERATIVO[estado]) as React.CSSProperties;

import { useState } from 'react';
import { colorDe, variable, type ColorFachada } from '../lib/colores';

/** Color de puertas y postigos que contrasta con el muro. */
const DETALLE: Record<ColorFachada, string> = {
  balcon: 'var(--maiz)',
  cobalto: 'var(--maiz)',
  cafetal: 'var(--maiz)',
  maiz: 'var(--cobalto)',
  lila: 'var(--maiz)',
  aguamarina: 'var(--balcon)',
};

const TINTA = 'var(--trazo, var(--tinta))';
const CAL = 'var(--ventana, var(--cal))';

/** Piso del apartamento según su identificación (SUQ-201 → 2). */
function pisoDe(identificacion: string) {
  const numero = identificacion.match(/(\d)\d{2}$/);
  return numero ? Math.min(Math.max(Number(numero[1]), 1), 3) : 1;
}

interface Props {
  identificacion: string;
  dormitorios?: number;
  /** Muestra la foto si carga; si no, queda la fachada dibujada. */
  imagen?: string | null;
  titulo?: string;
  className?: string;
}

/**
 * Fachada de bahareque en capas planas: losa de tinta, muro de color, alero, balcón del piso del
 * apartamento y puerta. Es la ilustración de cada apartamento cuando no hay foto disponible.
 */
export function Fachada({ identificacion, dormitorios = 1, imagen, titulo, className }: Props) {
  const [fotoFallo, setFotoFallo] = useState(false);
  const color = colorDe(identificacion);
  const muro = variable(color);
  const detalle = DETALLE[color];
  const piso = pisoDe(identificacion);
  const ventanas = Math.min(Math.max(dormitorios, 1), 3);

  if (imagen && !fotoFallo) {
    return (
      <img
        src={imagen}
        alt={titulo ?? `Foto de ${identificacion}`}
        className={className}
        onError={() => setFotoFallo(true)}
        style={{ objectFit: 'cover', width: '100%', height: '100%' }}
      />
    );
  }

  // Tres pisos de 60 de alto sobre un zócalo; el piso 1 es el de abajo.
  const alturaPiso = 60;
  const yPiso = (n: number) => 40 + (3 - n) * alturaPiso;
  const anchoVentana = 120 / ventanas - 12;

  return (
    <svg
      viewBox="0 0 240 240"
      role="img"
      aria-label={titulo ?? `Fachada del apartamento ${identificacion}`}
      className={className}
      preserveAspectRatio="xMidYMax meet"
      style={{ display: 'block', width: '100%', height: '100%', background: 'var(--muro)' }}
    >
      {/* Losa de tinta desplazada: la capa de atrás */}
      <rect x="44" y="34" width="164" height="206" fill={TINTA} />
      {/* Muro */}
      <rect x="36" y="26" width="164" height="206" fill={muro} stroke={TINTA} strokeWidth="3" />
      {/* Alero */}
      <rect x="26" y="18" width="184" height="14" fill={TINTA} />
      {/* Zócalo */}
      <rect x="36" y="214" width="164" height="18" fill={detalle} stroke={TINTA} strokeWidth="3" />

      {[1, 2, 3].map((n) => {
        const y = yPiso(n);
        const esEste = n === piso;
        return (
          <g key={n}>
            {Array.from({ length: n === 1 ? ventanas - 1 || 1 : ventanas }).map((_, i) => {
              const total = n === 1 ? ventanas - 1 || 1 : ventanas;
              const ancho = n === 1 ? 50 / total - 4 : anchoVentana;
              const x0 = n === 1 ? 128 : 56;
              const x = x0 + i * (ancho + (n === 1 ? 8 : 12));
              return (
                <g key={i}>
                  <rect x={x} y={y + 10} width={ancho} height={34} fill={CAL} stroke={TINTA} strokeWidth="3" />
                  <rect x={x - 6} y={y + 10} width={6} height={34} fill={detalle} stroke={TINTA} strokeWidth="2" />
                  <rect x={x + ancho} y={y + 10} width={6} height={34} fill={detalle} stroke={TINTA} strokeWidth="2" />
                </g>
              );
            })}
            {esEste && n > 1 && (
              <g>
                {/* Balcón del apartamento: losa, baranda y pie */}
                <rect x="48" y={y + 44} width="140" height="8" fill={TINTA} />
                {Array.from({ length: 12 }).map((_, i) => (
                  <rect key={i} x={52 + i * 11.5} y={y + 30} width="3" height="14" fill={TINTA} />
                ))}
                <rect x="48" y={y + 28} width="140" height="4" fill={TINTA} />
              </g>
            )}
          </g>
        );
      })}

      {/* Puerta de dos hojas */}
      <rect x="60" y="152" width="48" height="62" fill={detalle} stroke={TINTA} strokeWidth="3" />
      <line x1="84" y1="152" x2="84" y2="214" stroke={TINTA} strokeWidth="3" />
      <rect x="56" y="146" width="56" height="8" fill={TINTA} />
    </svg>
  );
}

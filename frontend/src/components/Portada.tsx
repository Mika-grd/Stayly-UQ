import { useState } from 'react';
import { Fachada } from './Fachada';

/** Una cuadra de doce casas: los seis apartamentos dos veces, con alturas distintas. */
const CUADRA = [
  { id: 'SUQ-101', dormitorios: 1, alto: 150 },
  { id: 'SUQ-201', dormitorios: 2, alto: 196 },
  { id: 'SUQ-102', dormitorios: 1, alto: 138 },
  { id: 'SUQ-301', dormitorios: 3, alto: 184 },
  { id: 'SUQ-202', dormitorios: 2, alto: 162 },
  { id: 'SUQ-302', dormitorios: 3, alto: 206 },
  { id: 'SUQ-102', dormitorios: 1, alto: 172 },
  { id: 'SUQ-201', dormitorios: 2, alto: 146 },
  { id: 'SUQ-301', dormitorios: 3, alto: 200 },
  { id: 'SUQ-101', dormitorios: 1, alto: 160 },
  { id: 'SUQ-302', dormitorios: 3, alto: 178 },
  { id: 'SUQ-202', dormitorios: 2, alto: 190 },
];

/** Tres copias para que el bucle no se note en pantallas anchas; se anima un tercio. */
const COPIAS = [0, 1, 2];

function Cuadra() {
  return (
    <div className="paisaje__capa paisaje__capa--calle">
      {COPIAS.map((copia) => (
        <div className="cuadra" key={copia}>
          {CUADRA.map((casa, i) => (
            <div
              key={i}
              className="cuadra__casa"
              style={{ height: casa.alto, width: casa.alto, marginInline: -casa.alto * 0.1 }}
            >
              <Fachada identificacion={casa.id} dormitorios={casa.dormitorios} />
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

/** Perfil de montaña que empieza y termina a la misma altura para repetirse sin costura. */
function Cordillera({ tipo, perfil, palmas = [] }: { tipo: 'lejos' | 'cerca'; perfil: string; palmas?: [number, number][] }) {
  return (
    <div className={`paisaje__capa paisaje__capa--${tipo}`}>
      {COPIAS.map((copia) => (
        <svg key={copia} viewBox="0 0 1600 220" width="1600" height="220" preserveAspectRatio="none">
          <path d={perfil} />
          {palmas.map(([x, y], i) => (
            <g key={i} className="palma">
              <rect x={x - 2} y={y - 110} width="4" height="112" />
              {[-160, -125, -90, -55, -20].map((angulo) => (
                <ellipse key={angulo} cx={x + 15} cy={y - 110} rx="16" ry="4.5" transform={`rotate(${angulo} ${x} ${y - 110})`} />
              ))}
            </g>
          ))}
        </svg>
      ))}
    </div>
  );
}

const LEJOS =
  'M0 120 C 120 60, 220 50, 330 96 S 520 30, 660 80 S 880 120, 990 64 S 1220 20, 1350 90 S 1520 110, 1600 120 L1600 220 L0 220 Z';
const CERCA =
  'M0 160 C 140 110, 260 120, 380 150 S 600 90, 760 130 S 980 170, 1120 120 S 1400 120, 1600 160 L1600 220 L0 220 Z';
const PALMAS: [number, number][] = [
  [200, 132],
  [236, 128],
  [700, 122],
  [1180, 124],
  [1212, 122],
  [1480, 140],
];

/** El Willys del Quindío: carrocería plana, parabrisas abatible y ruedas que giran. */
function Willys() {
  return (
    <svg className="willys" viewBox="0 0 150 86" aria-hidden="true">
      <g className="willys__carroceria">
        <rect x="18" y="8" width="5" height="34" fill="var(--tinta)" />
        <rect x="70" y="14" width="34" height="5" fill="var(--tinta)" />
        <rect x="74" y="18" width="4" height="22" fill="var(--tinta)" />
        <rect x="8" y="38" width="132" height="26" rx="3" fill="var(--balcon)" stroke="var(--tinta)" strokeWidth="3" />
        <rect x="100" y="34" width="42" height="12" fill="var(--balcon)" stroke="var(--tinta)" strokeWidth="3" />
        <rect x="134" y="42" width="10" height="8" fill="var(--maiz)" stroke="var(--tinta)" strokeWidth="2" />
        <rect x="26" y="20" width="44" height="18" fill="var(--maiz)" stroke="var(--tinta)" strokeWidth="3" />
        <rect x="36" y="10" width="24" height="12" fill="var(--cafetal)" stroke="var(--tinta)" strokeWidth="3" />
      </g>
      {[38, 116].map((cx) => (
        <g key={cx} className="willys__rueda" style={{ transformOrigin: `${cx}px 66px` }}>
          <circle cx={cx} cy="66" r="16" fill="var(--tinta)" />
          <circle cx={cx} cy="66" r="7" fill="var(--cal)" />
          <rect x={cx - 1.5} y="52" width="3" height="10" fill="var(--cal)" />
        </g>
      ))}
    </svg>
  );
}

const VUELOS = [
  { top: 70, vuelo: '16s', retraso: '0s', escala: 1 },
  { top: 130, vuelo: '22s', retraso: '-7s', escala: 0.7 },
  { top: 40, vuelo: '19s', retraso: '-13s', escala: 0.55 },
];

/** Solo se ven en la versión HARSH. */
function Murcielagos() {
  return (
    <div className="murcielagos" aria-hidden="true">
      {VUELOS.map((v, i) => (
        <div
          key={i}
          className="murcielago"
          style={{ top: v.top, '--vuelo': v.vuelo, '--retraso': v.retraso, width: 46 * v.escala } as React.CSSProperties}
        >
          <svg viewBox="0 0 48 20">
            <path d="M24 6 C 22 2, 20 2, 19 5 C 14 0, 6 0, 0 4 C 5 5, 7 9, 6 13 C 9 10, 13 11, 15 14 C 17 11, 20 11, 22 13 L24 16 L26 13 C 28 11, 31 11, 33 14 C 35 11, 39 10, 42 13 C 41 9, 43 5, 48 4 C 42 0, 34 0, 29 5 C 28 2, 26 2, 24 6 Z" />
          </svg>
        </div>
      ))}
    </div>
  );
}

export function Portada({ children }: { children?: React.ReactNode }) {
  const [quieto, setQuieto] = useState(false);
  return (
    <section className={`portada${quieto ? ' portada--quieta' : ''}`}>
      <div className="portada__sol" aria-hidden="true" />
      <Murcielagos />
      <div className="contenedor portada__titulo">
        <h1>
          Armenia, a todo <em>color</em>.
        </h1>
        <p>Seis apartamentos en el Quindío</p>
      </div>
      <div className="paisaje" aria-hidden="true">
        <Cordillera tipo="lejos" perfil={LEJOS} />
        <Cordillera tipo="cerca" perfil={CERCA} palmas={PALMAS} />
        <Cuadra />
        <Willys />
        <div className="paisaje__via" />
      </div>
      <button
        type="button"
        className="portada__pausa"
        aria-pressed={quieto}
        aria-label={quieto ? 'Reanudar animación' : 'Pausar animación'}
        onClick={() => setQuieto((q) => !q)}
      >
        {quieto ? (
          <svg viewBox="0 0 16 16" aria-hidden="true"><path d="M4 2 L14 8 L4 14 Z" fill="currentColor" /></svg>
        ) : (
          <svg viewBox="0 0 16 16" aria-hidden="true"><rect x="3" y="2" width="4" height="12" fill="currentColor" /><rect x="9" y="2" width="4" height="12" fill="currentColor" /></svg>
        )}
      </button>
      {children}
    </section>
  );
}

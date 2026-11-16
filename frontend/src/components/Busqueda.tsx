import { useState, type FormEvent } from 'react';
import { hoy, restarAnios, sumarDias } from '../lib/formato';

export interface Criterios {
  entrada: string;
  salida: string;
  adultos: number;
  ninos: number;
}

export function criteriosPorDefecto(): Criterios {
  const entrada = sumarDias(hoy(), 14);
  return { entrada, salida: sumarDias(entrada, 3), adultos: 2, ninos: 0 };
}

export function criteriosDe(params: URLSearchParams): Criterios {
  const base = criteriosPorDefecto();
  const numero = (v: string | null, def: number) => (v && !Number.isNaN(Number(v)) ? Number(v) : def);
  return {
    entrada: params.get('entrada') ?? base.entrada,
    salida: params.get('salida') ?? base.salida,
    adultos: numero(params.get('adultos'), base.adultos),
    ninos: numero(params.get('ninos'), base.ninos),
  };
}

export const aParametros = (c: Criterios) =>
  new URLSearchParams({ entrada: c.entrada, salida: c.salida, adultos: String(c.adultos), ninos: String(c.ninos) });

/**
 * La búsqueda pide fechas de nacimiento (el dominio decide quién paga por la edad). Para buscar
 * basta una aproximación: adultos de 30 años y niños de 6. Al reservar se piden las fechas reales.
 */
export function fechasNacimientoAproximadas(c: Criterios): string[] {
  const base = hoy();
  return [
    ...Array.from({ length: c.adultos }, () => restarAnios(base, 30)),
    ...Array.from({ length: c.ninos }, () => restarAnios(base, 6)),
  ];
}

export function Busqueda({ inicial, alBuscar }: { inicial: Criterios; alBuscar: (c: Criterios) => void }) {
  const [c, setC] = useState(inicial);

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    alBuscar(c);
  };

  return (
    <form className="busqueda placa placa--triple" onSubmit={enviar} style={{ '--losa': 'var(--maiz)' } as React.CSSProperties}>
      <label className="campo">
        <span>Llegada</span>
        <input
          type="date"
          required
          min={hoy()}
          value={c.entrada}
          onChange={(e) => {
            const entrada = e.target.value;
            setC((v) => ({ ...v, entrada, salida: v.salida <= entrada ? sumarDias(entrada, 1) : v.salida }));
          }}
        />
      </label>
      <label className="campo">
        <span>Salida</span>
        <input
          type="date"
          required
          min={sumarDias(c.entrada, 1)}
          value={c.salida}
          onChange={(e) => setC((v) => ({ ...v, salida: e.target.value }))}
        />
      </label>
      <label className="campo">
        <span>Adultos</span>
        <input
          type="number"
          min={1}
          max={6}
          required
          value={c.adultos}
          onChange={(e) => setC((v) => ({ ...v, adultos: Number(e.target.value) }))}
        />
      </label>
      <label className="campo">
        <span>Niños</span>
        <input
          type="number"
          min={0}
          max={5}
          value={c.ninos}
          onChange={(e) => setC((v) => ({ ...v, ninos: Number(e.target.value) }))}
        />
      </label>
      <button type="submit" className="boton boton--principal busqueda__boton">
        Buscar
      </button>
    </form>
  );
}

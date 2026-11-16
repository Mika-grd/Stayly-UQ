import { useState } from 'react';
import { apartamentos } from '../api/servicios';
import { aFecha, hoy, iso } from '../lib/formato';
import { useCarga } from '../lib/useCarga';
import { AvisoError, Cargando } from './Comunes';

const mes = new Intl.DateTimeFormat('es-CO', { month: 'long', year: 'numeric' });
/** "diciembre de 2026" → "Diciembre de 2026" (solo la primera letra). */
const nombreMes = (f: string) => {
  const t = mes.format(aFecha(f));
  return t.charAt(0).toUpperCase() + t.slice(1);
};
const DIAS = ['lu', 'ma', 'mi', 'ju', 'vi', 'sá', 'do'];

function primeroDelMes(fechaIso: string) {
  const f = aFecha(fechaIso);
  return iso(new Date(f.getFullYear(), f.getMonth(), 1));
}

function sumarMeses(fechaIso: string, meses: number) {
  const f = aFecha(fechaIso);
  return iso(new Date(f.getFullYear(), f.getMonth() + meses, 1));
}

/** Celdas del mes: huecos al inicio para que el lunes quede en la primera columna. */
function celdas(inicio: string) {
  const f = aFecha(inicio);
  const hueco = (f.getDay() + 6) % 7;
  const total = new Date(f.getFullYear(), f.getMonth() + 1, 0).getDate();
  return [
    ...Array.from({ length: hueco }, () => null),
    ...Array.from({ length: total }, (_, i) => iso(new Date(f.getFullYear(), f.getMonth(), i + 1))),
  ];
}

/**
 * Calendario de disponibilidad del detalle (7.1): dos meses, cada noche libre u ocupada según el
 * API. Un clic en una noche libre fija la llegada; el siguiente, la salida.
 */
export function CalendarioDisponibilidad({
  identificacion,
  entrada,
  salida,
  alElegir,
}: {
  identificacion: string;
  entrada: string;
  salida: string;
  alElegir: (entrada: string, salida: string) => void;
}) {
  const [inicio, setInicio] = useState(() => primeroDelMes(entrada < hoy() ? hoy() : entrada));
  const [eligiendoSalida, setEligiendoSalida] = useState(false);
  const fin = sumarMeses(inicio, 2);
  const { datos, error, cargando } = useCarga(
    () => apartamentos.disponibilidad(identificacion, inicio, fin),
    [identificacion, inicio],
  );
  const libre = new Map(datos?.noches.map((n) => [n.fecha, n.disponible]) ?? []);

  const elegir = (fecha: string) => {
    if (eligiendoSalida && fecha > entrada) {
      alElegir(entrada, fecha);
      setEligiendoSalida(false);
    } else if (libre.get(fecha)) {
      const siguiente = iso(new Date(aFecha(fecha).getTime() + 86_400_000));
      alElegir(fecha, siguiente);
      setEligiendoSalida(true);
    }
  };

  return (
    <div className="disponibilidad">
      <div className="disponibilidad__barra">
        <button
          type="button"
          className="boton boton--claro"
          disabled={inicio <= primeroDelMes(hoy())}
          onClick={() => setInicio(sumarMeses(inicio, -1))}
          aria-label="Mes anterior"
        >
          ‹
        </button>
        <span className="nota">{eligiendoSalida ? 'Elija la salida' : 'Elija la llegada'}</span>
        <button type="button" className="boton boton--claro" onClick={() => setInicio(sumarMeses(inicio, 1))} aria-label="Mes siguiente">
          ›
        </button>
      </div>
      <AvisoError error={error} />
      {cargando && !datos && <Cargando />}
      <div className="disponibilidad__meses">
        {[inicio, sumarMeses(inicio, 1)].map((m) => (
          <div key={m} className="disponibilidad__mes">
            <h3>{nombreMes(m)}</h3>
            <div className="disponibilidad__rejilla" role="grid">
              {DIAS.map((d) => (
                <span key={d} className="disponibilidad__dia" aria-hidden="true">
                  {d}
                </span>
              ))}
              {celdas(m).map((fecha, i) => {
                if (!fecha) return <span key={`h${i}`} />;
                const disponible = libre.get(fecha) ?? false;
                const elegida = fecha >= entrada && fecha < salida;
                const esSalida = fecha === salida;
                const seleccionable = disponible || (eligiendoSalida && fecha > entrada);
                return (
                  <button
                    key={fecha}
                    type="button"
                    className={`noche${disponible ? ' noche--libre' : ' noche--ocupada'}${elegida ? ' noche--elegida' : ''}${esSalida ? ' noche--salida' : ''}`}
                    disabled={!seleccionable}
                    onClick={() => elegir(fecha)}
                    aria-label={`${fecha}: ${disponible ? 'disponible' : 'no disponible'}`}
                    aria-pressed={elegida}
                  >
                    {aFecha(fecha).getDate()}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
      <div className="disponibilidad__leyenda nota">
        <span><i className="noche noche--libre" /> Libre</span>
        <span><i className="noche noche--ocupada" /> No disponible</span>
        <span><i className="noche noche--elegida" /> Su estancia</span>
      </div>
    </div>
  );
}

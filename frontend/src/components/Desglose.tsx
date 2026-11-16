import { useState } from 'react';
import type { CargoNoche } from '../api/tipos';
import { dinero, fechaCorta } from '../lib/formato';

/** Desglose noche por noche de una cotización (7.4): fecha, temporada, tarifa, quiénes pagan y subtotal. */
export function Desglose({ noches, abierto = false }: { noches: CargoNoche[]; abierto?: boolean }) {
  const [ver, setVer] = useState(abierto);
  return (
    <div className="desglose">
      <button type="button" className="boton boton--texto" onClick={() => setVer((v) => !v)} aria-expanded={ver}>
        {ver ? 'Ocultar noches' : 'Precio por noche'}
      </button>
      {ver && (
        <div className="tabla-envoltura">
          <table className="tabla tabla--compacta">
            <thead>
              <tr>
                <th>Noche</th>
                <th>Temporada</th>
                <th className="num">Tarifa</th>
                <th className="num">Pagan</th>
                <th className="num">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {noches.map((n) => (
                <tr key={n.fecha}>
                  <td>{fechaCorta(n.fecha)}</td>
                  <td>{n.temporada}</td>
                  <td className="num">{dinero(n.tarifa)}</td>
                  <td className="num">{n.ocupantesFacturables}</td>
                  <td className="num">{dinero(n.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

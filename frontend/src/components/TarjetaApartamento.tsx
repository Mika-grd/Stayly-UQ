import { Link } from 'react-router-dom';
import type { ApartamentoResumen } from '../api/tipos';
import { colorDe, variable } from '../lib/colores';
import { dinero } from '../lib/formato';

/** Un único alojamiento (F-13): todas las tarjetas comparten la ubicación del mapa de la búsqueda. */
const LUGAR = 'Norte de Armenia, Quindío';
import { Fachada } from './Fachada';

export function TarjetaApartamento({
  apartamento,
  noches,
  parametros,
}: {
  apartamento: ApartamentoResumen;
  noches: number;
  parametros: URLSearchParams;
}) {
  const losa = variable(colorDe(apartamento.identificacion));
  return (
    <Link
      to={`/apartamentos/${apartamento.identificacion}?${parametros}`}
      className="tarjeta placa"
      style={{ '--losa': losa } as React.CSSProperties}
    >
      <div className="tarjeta__imagen">
        <Fachada
          identificacion={apartamento.identificacion}
          dormitorios={apartamento.dormitorios}
          imagen={apartamento.imagenPrincipal}
          titulo={apartamento.nombre}
        />
      </div>
      <div className="tarjeta__cuerpo">
        <h3>{apartamento.nombre}</h3>
        <p className="tarjeta__datos">
          {apartamento.dormitorios} {apartamento.dormitorios === 1 ? 'dormitorio' : 'dormitorios'}, hasta{' '}
          {apartamento.capacidad} personas
          {apartamento.admiteMascotas ? ', admite mascotas' : ''}
        </p>
        <p className="tarjeta__lugar">
          <svg viewBox="0 0 12 16" aria-hidden="true">
            <path d="M6 0a6 6 0 0 0-6 6c0 4.5 6 10 6 10s6-5.5 6-10a6 6 0 0 0-6-6Zm0 8.5A2.5 2.5 0 1 1 6 3.5a2.5 2.5 0 0 1 0 5Z" />
          </svg>
          {LUGAR}
        </p>
        {apartamento.advertenciaAcceso && <p className="tarjeta__advertencia">{apartamento.advertenciaAcceso}</p>}
        <p className="tarjeta__precio">
          <span className="cifra">{dinero(apartamento.valorEstimado)}</span>
          <span> por {noches} {noches === 1 ? 'noche' : 'noches'}</span>
        </p>
      </div>
    </Link>
  );
}

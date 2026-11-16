import { Link } from 'react-router-dom';
import type { ApartamentoResumen } from '../api/tipos';
import { colorDe, variable } from '../lib/colores';
import { dinero } from '../lib/formato';
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
        {apartamento.advertenciaAcceso && <p className="tarjeta__advertencia">{apartamento.advertenciaAcceso}</p>}
        <p className="tarjeta__precio">
          <span className="cifra">{dinero(apartamento.valorEstimado)}</span>
          <span> por {noches} {noches === 1 ? 'noche' : 'noches'}</span>
        </p>
      </div>
    </Link>
  );
}

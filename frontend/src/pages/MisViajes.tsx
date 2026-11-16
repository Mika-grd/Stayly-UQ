import { useState } from 'react';
import { Link } from 'react-router-dom';
import { reservas } from '../api/servicios';
import { AvisoError, Cargando, EstadoReservaChip, Paginacion } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { colorDe, variable } from '../lib/colores';
import { dinero, rango } from '../lib/formato';
import { useCarga } from '../lib/useCarga';

export function MisViajes() {
  const [pagina, setPagina] = useState(0);
  const { datos, error, cargando } = useCarga(() => reservas.mias(pagina), [pagina]);

  return (
    <main className="contenedor pagina">
      <h1>Mis viajes</h1>
      <AvisoError error={error} />
      {cargando && <Cargando />}
      {datos && datos.contenido.length === 0 && (
        <div className="vacio">
          <h3>Sin viajes</h3>
          <Link to="/" className="boton boton--principal">
            Buscar apartamento
          </Link>
        </div>
      )}
      <ul className="viajes">
        {datos?.contenido.map((r) => (
          <li key={r.codigo}>
            <Link
              to={`/reservas/${r.codigo}`}
              className="viaje placa placa--chica"
              style={{ '--losa': variable(colorDe(r.identificacionApartamento)) } as React.CSSProperties}
            >
              <div className="viaje__fachada">
                <Fachada identificacion={r.identificacionApartamento} />
              </div>
              <div className="viaje__texto">
                <h3>{rango(r.fechaEntrada, r.fechaSalida)}</h3>
                <p>
                  Apartamento {r.identificacionApartamento}, reserva {r.codigo}
                </p>
                <EstadoReservaChip estado={r.estado} />
              </div>
              <span className="cifra viaje__total">{dinero(r.total)}</span>
            </Link>
          </li>
        ))}
      </ul>
      {datos && (
        <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} totalElementos={datos.totalElementos} alCambiar={setPagina} />
      )}
    </main>
  );
}

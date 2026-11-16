import { useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apartamentos } from '../api/servicios';
import { aParametros, Busqueda, criteriosDe, fechasNacimientoAproximadas, type Criterios } from '../components/Busqueda';
import { AvisoError, Cargando, Paginacion } from '../components/Comunes';
import { Portada } from '../components/Portada';
import { TarjetaApartamento } from '../components/TarjetaApartamento';
import { noches, rango } from '../lib/formato';
import { useCarga } from '../lib/useCarga';

export function Inicio() {
  const [params, setParams] = useSearchParams();
  const criterios = useMemo(() => criteriosDe(params), [params]);
  const pagina = Number(params.get('pagina') ?? 0);
  const totalNoches = noches(criterios.entrada, criterios.salida);
  const enlace = aParametros(criterios);

  const { datos, error, cargando } = useCarga(
    () =>
      apartamentos.disponibles({
        entrada: criterios.entrada,
        salida: criterios.salida,
        fechasNacimiento: fechasNacimientoAproximadas(criterios),
        pagina,
      }),
    [criterios.entrada, criterios.salida, criterios.adultos, criterios.ninos, pagina],
  );

  const buscar = (c: Criterios) => setParams(aParametros(c));
  const personas = criterios.adultos + criterios.ninos;

  return (
    <main>
      <Portada>
        <div className="contenedor portada__busqueda">
          <Busqueda key={params.toString()} inicial={criterios} alBuscar={buscar} />
        </div>
      </Portada>

      <section className="contenedor resultados" aria-live="polite">
        <div className="resultados__cabeza">
          <h2>
            {datos
              ? `${datos.totalElementos} ${datos.totalElementos === 1 ? 'apartamento libre' : 'apartamentos libres'}`
              : 'Apartamentos libres'}
          </h2>
          <p>
            Del {rango(criterios.entrada, criterios.salida)}, {totalNoches} {totalNoches === 1 ? 'noche' : 'noches'},{' '}
            {personas} {personas === 1 ? 'persona' : 'personas'}.
          </p>
        </div>

        <AvisoError error={error} />
        {cargando && <Cargando />}
        {datos && datos.contenido.length === 0 && (
          <div className="vacio">
            <h3>Sin disponibilidad</h3>
          </div>
        )}
        {datos && datos.contenido.length > 0 && (
          <div className="rejilla">
            {datos.contenido.map((apto) => (
              <TarjetaApartamento key={apto.identificacion} apartamento={apto} noches={totalNoches} parametros={enlace} />
            ))}
          </div>
        )}
        {datos && (
          <Paginacion
            pagina={datos.pagina}
            totalPaginas={datos.totalPaginas}
            totalElementos={datos.totalElementos}
            alCambiar={(p) => {
              const nuevos = aParametros(criterios);
              nuevos.set('pagina', String(p));
              setParams(nuevos);
            }}
          />
        )}
      </section>
    </main>
  );
}

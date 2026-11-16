import { useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apartamentos } from '../api/servicios';
import { aParametros, Busqueda, criteriosDe, fechasNacimientoAproximadas, type Criterios } from '../components/Busqueda';
import { AvisoError, Cargando, Paginacion } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { TarjetaApartamento } from '../components/TarjetaApartamento';
import { noches, rango } from '../lib/formato';
import { useCarga } from '../lib/useCarga';

/** Los seis de la Ficha, de distinta altura, como una cuadra de Salento. */
const CALLE = [
  { id: 'SUQ-101', dormitorios: 1, alto: 78 },
  { id: 'SUQ-201', dormitorios: 2, alto: 100 },
  { id: 'SUQ-102', dormitorios: 1, alto: 70 },
  { id: 'SUQ-301', dormitorios: 3, alto: 92 },
  { id: 'SUQ-202', dormitorios: 2, alto: 84 },
  { id: 'SUQ-302', dormitorios: 3, alto: 96 },
];

function Calle() {
  return (
    <div className="calle" aria-hidden="true">
      {CALLE.map((casa) => (
        <div key={casa.id} className="calle__casa" style={{ height: `${casa.alto}%` }}>
          <Fachada identificacion={casa.id} dormitorios={casa.dormitorios} />
        </div>
      ))}
    </div>
  );
}

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
      <section className="portada">
        <div className="contenedor portada__rejilla">
          <h1>Seis apartamentos en Armenia, Quindío</h1>
          <p className="portada__bajada">
            Elija fechas y grupo: verá qué apartamentos están libres y el precio de toda la estancia, noche por noche.
          </p>
        </div>
        <div className="contenedor">
          <Calle />
        </div>
        <div className="contenedor portada__busqueda">
          <Busqueda key={params.toString()} inicial={criterios} alBuscar={buscar} />
        </div>
      </section>

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
        {cargando && <Cargando texto="Buscando apartamentos libres…" />}
        {datos && datos.contenido.length === 0 && (
          <div className="vacio">
            <h3>No hay apartamentos libres para esas fechas y ese grupo</h3>
            <p>Pruebe otras fechas o divida el grupo: cada apartamento tiene su capacidad máxima.</p>
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

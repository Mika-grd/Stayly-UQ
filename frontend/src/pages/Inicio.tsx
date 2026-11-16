import { useMemo, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apartamentos } from '../api/servicios';
import { aParametros, Busqueda, criteriosDe, fechasNacimientoAproximadas, type Criterios } from '../components/Busqueda';
import { AvisoError, Cargando, Paginacion } from '../components/Comunes';
import { Mapa } from '../components/Mapa';
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

  // Filtros de 7.5: capacidad, precio máximo y características (los aplica el API)
  const capacidad = params.get('capacidad') ? Number(params.get('capacidad')) : null;
  const precioMax = params.get('precioMax') ? Number(params.get('precioMax')) : null;
  const elegidas = params.getAll('caracteristica');

  const { datos, error, cargando } = useCarga(
    () =>
      apartamentos.disponibles({
        entrada: criterios.entrada,
        salida: criterios.salida,
        fechasNacimiento: fechasNacimientoAproximadas(criterios),
        capacidadMinima: capacidad,
        precioMaximo: precioMax,
        caracteristicas: elegidas,
        pagina,
      }),
    [criterios.entrada, criterios.salida, criterios.adultos, criterios.ninos, capacidad, precioMax, elegidas.join('|'), pagina],
  );

  // Las características conocidas se acumulan para que filtrar no haga desaparecer las opciones
  const conocidas = useRef(new Set<string>());
  datos?.contenido.forEach((a) => a.caracteristicas.forEach((c) => conocidas.current.add(c)));
  elegidas.forEach((c) => conocidas.current.add(c));
  const opciones = [...conocidas.current].sort((a, b) => a.localeCompare(b, 'es'));

  // La ubicación es la del alojamiento (un único alojamiento, F-13): basta el detalle del primero
  const primero = datos?.contenido[0]?.identificacion;
  const ubicacion = useCarga(() => (primero ? apartamentos.detalle(primero) : Promise.resolve(null)), [primero]);

  const conFiltros = (cambio: (p: URLSearchParams) => void) => {
    const nuevos = new URLSearchParams(params);
    nuevos.delete('pagina');
    cambio(nuevos);
    setParams(nuevos, { replace: true });
  };
  const fijar = (clave: string, valor: string) => conFiltros((p) => (valor ? p.set(clave, valor) : p.delete(clave)));
  const alternar = (c: string) =>
    conFiltros((p) => {
      const actuales = p.getAll('caracteristica');
      p.delete('caracteristica');
      (actuales.includes(c) ? actuales.filter((x) => x !== c) : [...actuales, c]).forEach((x) => p.append('caracteristica', x));
    });
  const hayFiltros = capacidad != null || precioMax != null || elegidas.length > 0;

  const buscar = (c: Criterios) =>
    setParams(() => {
      const nuevos = aParametros(c);
      ['capacidad', 'precioMax'].forEach((k) => params.get(k) && nuevos.set(k, params.get(k)!));
      elegidas.forEach((x) => nuevos.append('caracteristica', x));
      return nuevos;
    });
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

        <form className="filtros" onSubmit={(e) => e.preventDefault()} aria-label="Filtros">
          <label className="campo">
            <span>Capacidad mínima</span>
            <input type="number" min={1} max={10} value={capacidad ?? ''} placeholder="Cualquiera" onChange={(e) => fijar('capacidad', e.target.value)} />
          </label>
          <label className="campo">
            <span>Precio máximo de la estancia</span>
            <input type="number" min={0} step={50000} value={precioMax ?? ''} placeholder="Sin tope" onChange={(e) => fijar('precioMax', e.target.value)} />
          </label>
          {opciones.length > 0 && (
            <fieldset className="filtros__caracteristicas">
              <legend>Características</legend>
              {opciones.map((c) => (
                <button key={c} type="button" className="chip chip--filtro" aria-pressed={elegidas.includes(c)} onClick={() => alternar(c)}>
                  {c}
                </button>
              ))}
            </fieldset>
          )}
          {hayFiltros && (
            <button
              type="button"
              className="boton boton--texto"
              onClick={() => conFiltros((p) => ['capacidad', 'precioMax', 'caracteristica'].forEach((k) => p.delete(k)))}
            >
              Quitar filtros
            </button>
          )}
        </form>

        <AvisoError error={error} />
        {cargando && <Cargando />}
        {datos && datos.contenido.length === 0 && (
          <div className="vacio">
            <h3>{hayFiltros ? 'Ningún apartamento cumple los filtros' : 'Sin disponibilidad'}</h3>
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
              const nuevos = new URLSearchParams(params);
              nuevos.set('pagina', String(p));
              setParams(nuevos);
            }}
          />
        )}
        {ubicacion.datos?.ubicacion && (
          <section className="resultados__mapa">
            <h2>Ubicación</h2>
            <Mapa ubicacion={ubicacion.datos.ubicacion} titulo="Stayly-UQ" />
          </section>
        )}
      </section>
    </main>
  );
}

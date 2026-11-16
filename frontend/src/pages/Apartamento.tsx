import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { apartamentos, reservas } from '../api/servicios';
import { useSesion } from '../auth/Sesion';
import { aParametros, criteriosDe, fechasNacimientoAproximadas, type Criterios } from '../components/Busqueda';
import { AvisoError, Cargando } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { colorDe, variable } from '../lib/colores';
import { dinero, fechaCorta, hoy, noches, sumarDias } from '../lib/formato';
import { useCarga } from '../lib/useCarga';

export function Apartamento() {
  const { id = '' } = useParams();
  const [params, setParams] = useSearchParams();
  const criterios = useMemo(() => criteriosDe(params), [params]);
  const { es } = useSesion();
  const navegar = useNavigate();
  const [verNoches, setVerNoches] = useState(false);

  const detalle = useCarga(() => apartamentos.detalle(id), [id]);
  const politica = useCarga(() => reservas.politicaVigente(), []);

  // Disponibilidad y precio estimado: la búsqueda pública dice si está libre para ese grupo.
  const disponible = useCarga(async () => {
    const pagina = await apartamentos.disponibles({
      entrada: criterios.entrada,
      salida: criterios.salida,
      fechasNacimiento: fechasNacimientoAproximadas(criterios),
    });
    return pagina.contenido.find((a) => a.identificacion === id) ?? null;
  }, [id, criterios.entrada, criterios.salida, criterios.adultos, criterios.ninos]);

  // Con sesión, el desglose noche por noche de la cotización (CU-01).
  const puedeCotizar = es('HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR');
  const cotizacion = useCarga(
    () =>
      puedeCotizar && disponible.datos
        ? reservas.cotizar({
            apartamento: id,
            entrada: criterios.entrada,
            salida: criterios.salida,
            fechasNacimiento: fechasNacimientoAproximadas(criterios),
            mascotas: 0,
          })
        : Promise.resolve(null),
    [puedeCotizar, disponible.datos, id, criterios.entrada, criterios.salida, criterios.adultos, criterios.ninos],
  );

  const cambiar = (cambio: Partial<Criterios>) => setParams(aParametros({ ...criterios, ...cambio }), { replace: true });

  if (detalle.cargando) return <main className="contenedor pagina"><Cargando /></main>;
  if (detalle.error || !detalle.datos) {
    return (
      <main className="contenedor pagina">
        <AvisoError error={detalle.error} />
        <Link to="/">Volver a la búsqueda</Link>
      </main>
    );
  }

  const apto = detalle.datos;
  const losa = variable(colorDe(apto.identificacion));
  const totalNoches = noches(criterios.entrada, criterios.salida);
  const total = cotizacion.datos?.total ?? disponible.datos?.valorEstimado ?? null;
  const principal = apto.imagenes.find((i) => i.principal)?.url ?? apto.imagenes[0]?.url;

  return (
    <main className="contenedor pagina">
      <Link to={`/?${aParametros(criterios)}`} className="volver">
        Volver a los resultados
      </Link>

      <header className="ficha__cabeza">
        <h1>{apto.nombre}</h1>
        <p>
          {apto.dormitorios} {apto.dormitorios === 1 ? 'dormitorio' : 'dormitorios'}, hasta {apto.capacidad} personas
          {apto.admiteMascotas ? ', admite mascotas' : ', sin mascotas'}.
        </p>
      </header>

      <div className="galeria placa" style={{ '--losa': losa } as React.CSSProperties}>
        <div className="galeria__principal">
          <Fachada identificacion={apto.identificacion} dormitorios={apto.dormitorios} imagen={principal} titulo={apto.nombre} />
        </div>
        <div className="galeria__lado" style={{ background: losa }}>
          <span className="galeria__codigo">{apto.identificacion}</span>
        </div>
      </div>

      <div className="ficha">
        <div className="ficha__texto">
          <section>
            <h2>Descripción</h2>
            <p>{apto.descripcion}</p>
            {apto.advertenciaAcceso && (
              <div className="aviso aviso--info">
                <strong>Antes de reservar</strong>
                {apto.advertenciaAcceso}
              </div>
            )}
          </section>

          {apto.caracteristicas.length > 0 && (
            <section>
              <h2>Comodidades</h2>
              <ul className="lista-marcada">
                {apto.caracteristicas.map((c) => (
                  <li key={c}>{c}</li>
                ))}
              </ul>
            </section>
          )}

          <section>
            <h2>Cancelación</h2>
            {politica.datos ? (
              <>
                <ul className="tramos">
                  {[...politica.datos.tramos]
                    .sort((a, b) => b.antelacionMinimaDias - a.antelacionMinimaDias)
                    .map((t, i, todos) => (
                      <li key={t.antelacionMinimaDias}>
                        <span className="cifra">{t.porcentajeRetencion}%</span>
                        <span>
                          {t.antelacionMinimaDias === 0
                            ? `menos de ${todos[i - 1]?.antelacionMinimaDias ?? 1} días antes`
                            : `${t.antelacionMinimaDias} días antes o más`}
                        </span>
                      </li>
                    ))}
                  <li>
                    <span className="cifra">{politica.datos.porcentajeRetencionNoShow}%</span>
                    <span>si no llega</span>
                  </li>
                </ul>
              </>
            ) : (
              <Cargando />
            )}
          </section>
        </div>

        <aside className="reservar placa placa--triple" style={{ '--losa': losa } as React.CSSProperties}>
          <p className="reservar__precio">
            <span className="cifra">{dinero(total)}</span>
            <span>
              {' '}
              por {totalNoches} {totalNoches === 1 ? 'noche' : 'noches'}
            </span>
          </p>

          <div className="fila fila--dos">
            <label className="campo">
              <span>Llegada</span>
              <input
                type="date"
                min={hoy()}
                value={criterios.entrada}
                onChange={(e) =>
                  cambiar({
                    entrada: e.target.value,
                    salida: criterios.salida <= e.target.value ? sumarDias(e.target.value, 1) : criterios.salida,
                  })
                }
              />
            </label>
            <label className="campo">
              <span>Salida</span>
              <input
                type="date"
                min={sumarDias(criterios.entrada, 1)}
                value={criterios.salida}
                onChange={(e) => cambiar({ salida: e.target.value })}
              />
            </label>
            <label className="campo">
              <span>Adultos</span>
              <input
                type="number"
                min={1}
                max={apto.capacidad}
                value={criterios.adultos}
                onChange={(e) => cambiar({ adultos: Number(e.target.value) })}
              />
            </label>
            <label className="campo">
              <span>Niños</span>
              <input
                type="number"
                min={0}
                max={apto.capacidad}
                value={criterios.ninos}
                onChange={(e) => cambiar({ ninos: Number(e.target.value) })}
              />
            </label>
          </div>

          {disponible.cargando && <Cargando />}
          <AvisoError error={disponible.error} />
          {!disponible.cargando && !disponible.error && !disponible.datos && (
            <div className="aviso aviso--error">
              <strong>No está libre para esas fechas o ese grupo.</strong>
              Pruebe otras fechas o vuelva a la búsqueda para ver los apartamentos libres.
            </div>
          )}

          {disponible.datos && (
            <>
              <button
                className="boton boton--principal boton--ancho"
                onClick={() => navegar(`/reservar/${apto.identificacion}?${aParametros(criterios)}`)}
              >
                Reservar
              </button>
            </>
          )}

          {cotizacion.datos && (
            <div className="desglose">
              <button className="boton boton--texto" onClick={() => setVerNoches((v) => !v)} aria-expanded={verNoches}>
                {verNoches ? 'Ocultar noches' : 'Precio por noche'}
              </button>
              {verNoches && (
                <table className="tabla">
                  <tbody>
                    {cotizacion.datos.noches.map((n) => (
                      <tr key={n.fecha}>
                        <td>{fechaCorta(n.fecha)}</td>
                        <td>{n.temporada}</td>
                        <td className="num">{dinero(n.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              <p className="nota">
                {cotizacion.datos.ocupantesFacturables} de {cotizacion.datos.ocupantesTotales} personas facturables
              </p>
            </div>
          )}
        </aside>
      </div>
    </main>
  );
}

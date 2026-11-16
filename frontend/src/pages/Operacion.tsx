import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { apartamentos, operacion } from '../api/servicios';
import type { ApartamentoDetalle, GravedadNovedad } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { AvisoError, AvisoOk, Cargando, EstadoOperativoChip, Paginacion } from '../components/Comunes';
import { colorDe, textoSobre, variable } from '../lib/colores';
import { aFecha, fechaHora, hoy, sumarDias } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';

function PorPreparar({ alVerNovedades, alCargar }: { alVerNovedades: (id: string) => void; alCargar: (ids: string[]) => void }) {
  const { es } = useSesion();
  const puedePreparar = es('PERSONAL_SERVICIO', 'ADMINISTRADOR');
  const carga = useCarga(() => apartamentos.porPreparar(), []);
  const accion = useAccion();
  useEffect(() => {
    if (carga.datos) alCargar(carga.datos.map((a) => a.identificacion));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [carga.datos]);
  const [mensaje, setMensaje] = useState<string | null>(null);

  const cambiar = async (operacionApi: () => Promise<unknown>, texto: string) => {
    setMensaje(null);
    const ok = await accion.ejecutar(async () => {
      await operacionApi();
      return true;
    });
    if (ok) {
      setMensaje(texto);
      carga.recargar();
    }
  };

  return (
    <section className="placa" style={{ '--losa': 'var(--maiz)' } as React.CSSProperties}>
      <h2>Por preparar</h2>
      {mensaje && <AvisoOk>{mensaje}</AvisoOk>}
      <AvisoError error={carga.error ?? accion.error} />
      {carga.cargando && <Cargando />}
      {carga.datos && carga.datos.length === 0 && <p className="nota">Todo listo.</p>}
      <ul className="preparar">
        {carga.datos?.map((a) => (
          <li key={a.identificacion}>
            <span className="movimiento__apto" style={{ background: variable(colorDe(a.identificacion)), color: textoSobre(colorDe(a.identificacion)) }}>
              {a.identificacion.replace('SUQ-', '')}
            </span>
            <strong>{a.nombre}</strong>
            <EstadoOperativoChip estado={a.estadoOperativo} />
            {puedePreparar && a.estadoOperativo === 'PENDIENTE_PREPARACION' && (
              <button className="boton boton--maiz" disabled={accion.enCurso} onClick={() => cambiar(() => apartamentos.iniciarPreparacion(a.identificacion), `${a.nombre}: preparación iniciada.`)}>
                Iniciar preparación
              </button>
            )}
            {puedePreparar && a.estadoOperativo === 'EN_PREPARACION' && (
              <button className="boton boton--cafetal" disabled={accion.enCurso} onClick={() => cambiar(() => apartamentos.marcarPreparado(a.identificacion), `${a.nombre} quedó preparado.`)}>
                Marcar preparado
              </button>
            )}
            <button className="boton boton--texto" onClick={() => alVerNovedades(a.identificacion)}>
              Novedades
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}

function FilaApartamento({ id, alCambiar, alVerNovedades }: { id: string; alCambiar: () => void; alVerNovedades: (id: string) => void }) {
  const { es } = useSesion();
  const carga = useCarga(() => apartamentos.detalle(id), [id]);
  const accion = useAccion();
  const [motivo, setMotivo] = useState('');
  const [pidiendoMotivo, setPidiendoMotivo] = useState(false);
  const a: ApartamentoDetalle | null = carga.datos;

  const aplicar = async (op: () => Promise<unknown>) => {
    const ok = await accion.ejecutar(async () => {
      await op();
      return true;
    });
    if (ok) {
      setPidiendoMotivo(false);
      setMotivo('');
      carga.recargar();
      alCambiar();
    }
  };

  return (
    <li className="estado-apto">
      <span className="movimiento__apto" style={{ background: variable(colorDe(id)), color: textoSobre(colorDe(id)) }}>
        {id.replace('SUQ-', '')}
      </span>
      <strong>{a?.nombre ?? id}</strong>
      {a && <EstadoOperativoChip estado={a.estadoOperativo} />}
      {es('ADMINISTRADOR') && a && a.estadoOperativo !== 'FUERA_DE_SERVICIO' && !pidiendoMotivo && (
        <button className="boton boton--claro" onClick={() => setPidiendoMotivo(true)}>
          Sacar de servicio
        </button>
      )}
      {es('ADMINISTRADOR') && a?.estadoOperativo === 'FUERA_DE_SERVICIO' && (
        <button className="boton boton--cafetal" disabled={accion.enCurso} onClick={() => aplicar(() => apartamentos.volverAServicio(id))}>
          Volver a servicio
        </button>
      )}
      <button className="boton boton--texto" onClick={() => alVerNovedades(id)}>
        Novedades
      </button>
      {pidiendoMotivo && (
        <form
          className="fila fila--accion estado-apto__motivo"
          onSubmit={(e) => {
            e.preventDefault();
            aplicar(() => apartamentos.fueraDeServicio(id, motivo));
          }}
        >
          <label className="campo">
            <span>Motivo</span>
            <input required value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Daño, reparación…" />
          </label>
          <button className="boton boton--principal" disabled={accion.enCurso}>
            Sacar de servicio
          </button>
          <button type="button" className="boton boton--texto" onClick={() => setPidiendoMotivo(false)}>
            Volver
          </button>
        </form>
      )}
      <AvisoError error={accion.error} />
    </li>
  );
}

const DIAS = 14;
const dia = new Intl.DateTimeFormat('es-CO', { weekday: 'short' });
const numeroDia = new Intl.DateTimeFormat('es-CO', { day: 'numeric' });

function CalendarioOcupacion({ alVerNovedades, alCargar }: { alVerNovedades: (id: string) => void; alCargar: (ids: string[]) => void }) {
  const [desde, setDesde] = useState(hoy());
  const hasta = sumarDias(desde, DIAS);
  const [version, setVersion] = useState(0);
  const { datos, error, cargando } = useCarga(() => operacion.calendario(desde, hasta), [desde, version]);
  useEffect(() => {
    if (datos) alCargar(datos.apartamentos.map((a) => a.identificacion));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [datos]);

  return (
    <>
      <section className="placa" style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
        <div className="titulo-con-accion">
          <h2>Ocupación</h2>
          <div className="acciones acciones--fila">
            <button className="boton boton--claro" onClick={() => setDesde(sumarDias(desde, -7))}>
              Semana anterior
            </button>
            <button className="boton boton--claro" onClick={() => setDesde(hoy())}>
              Hoy
            </button>
            <button className="boton boton--claro" onClick={() => setDesde(sumarDias(desde, 7))}>
              Semana siguiente
            </button>
          </div>
        </div>
        <AvisoError error={error} />
        {cargando && <Cargando />}
        {datos && (
          <div className="calendario" style={{ '--dias': DIAS } as React.CSSProperties}>
            <div className="calendario__fila calendario__fila--cabeza">
              <span />
              {datos.apartamentos[0]?.noches.map((n) => (
                <span key={n.fecha} className={n.fecha === hoy() ? 'calendario__hoy' : undefined}>
                  <small>{dia.format(aFecha(n.fecha)).replace('.', '')}</small>
                  {numeroDia.format(aFecha(n.fecha))}
                </span>
              ))}
            </div>
            {datos.apartamentos.map((apto) => {
              const color = colorDe(apto.identificacion);
              return (
                <div className="calendario__fila" key={apto.identificacion}>
                  <span className="calendario__apto">{apto.identificacion}</span>
                  {apto.noches.map((n) =>
                    n.estado === 'RESERVADA' && n.codigoReserva ? (
                      <Link
                        key={n.fecha}
                        to={`/reservas/${n.codigoReserva}`}
                        className="calendario__noche calendario__noche--reservada"
                        style={{ background: variable(color) }}
                        title={`${n.codigoReserva}, noche del ${n.fecha}`}
                      >
                        <span className="sr-only">
                          {apto.identificacion}, {n.fecha}, reservada por {n.codigoReserva}
                        </span>
                      </Link>
                    ) : (
                      <span
                        key={n.fecha}
                        className={`calendario__noche calendario__noche--${n.estado.toLowerCase()}`}
                        title={`${n.fecha}: ${n.estado === 'LIBRE' ? 'libre' : 'bloqueada'}`}
                      >
                        <span className="sr-only">
                          {apto.identificacion}, {n.fecha}, {n.estado === 'LIBRE' ? 'libre' : 'bloqueada'}
                        </span>
                      </span>
                    ),
                  )}
                </div>
              );
            })}
            <div className="calendario__leyenda">
              <span><i className="calendario__noche calendario__noche--libre" /> Libre</span>
              <span><i className="calendario__noche" style={{ background: 'var(--cobalto)' }} /> Reservada</span>
              <span><i className="calendario__noche calendario__noche--bloqueada" /> Bloqueada</span>
            </div>
          </div>
        )}
      </section>

      {datos && (
        <section className="placa" style={{ '--losa': 'var(--cafetal)' } as React.CSSProperties}>
          <h2>Apartamentos</h2>
          <ul className="preparar">
            {datos.apartamentos.map((a) => (
              <FilaApartamento key={a.identificacion} id={a.identificacion} alCambiar={() => setVersion((v) => v + 1)} alVerNovedades={alVerNovedades} />
            ))}
          </ul>
        </section>
      )}
    </>
  );
}

const GRAVEDADES: { valor: GravedadNovedad; nombre: string }[] = [
  { valor: 'BAJA', nombre: 'Baja' },
  { valor: 'MEDIA', nombre: 'Media' },
  { valor: 'ALTA', nombre: 'Alta' },
  { valor: 'CRITICA', nombre: 'Crítica' },
];
const COLOR_GRAVEDAD: Record<GravedadNovedad, string> = { BAJA: 'muro', MEDIA: 'maiz', ALTA: 'balcon', CRITICA: 'lila' };

/** Novedades de un apartamento (7.6, CU-11 y CU-41): registrar con gravedad y consultar el historial. */
function Novedades({ apartamento, setApartamento, conocidos }: { apartamento: string; setApartamento: (id: string) => void; conocidos: string[] }) {
  const [texto, setTexto] = useState(apartamento);
  const [descripcion, setDescripcion] = useState('');
  const [gravedad, setGravedad] = useState<GravedadNovedad>('MEDIA');
  const [pagina, setPagina] = useState(0);
  const accion = useAccion();
  const [mensaje, setMensaje] = useState<string | null>(null);
  const historial = useCarga(
    () => (apartamento ? apartamentos.novedades(apartamento, pagina) : Promise.resolve(null)),
    [apartamento, pagina],
  );
  useEffect(() => {
    setTexto(apartamento);
    setPagina(0);
    setMensaje(null);
  }, [apartamento]);

  const registrar = async (e: React.FormEvent) => {
    e.preventDefault();
    setMensaje(null);
    const n = await accion.ejecutar(() => apartamentos.registrarNovedad(apartamento, descripcion, gravedad));
    if (n) {
      setDescripcion('');
      setMensaje('Novedad registrada.');
      setPagina(0);
      historial.recargar();
    }
  };

  return (
    <section id="novedades" className="placa" style={{ '--losa': 'var(--balcon)' } as React.CSSProperties}>
      <h2>Novedades</h2>
      <form
        className="barra"
        onSubmit={(e) => {
          e.preventDefault();
          setApartamento(texto.trim().toUpperCase());
        }}
      >
        <label className="campo">
          <span>Apartamento</span>
          <input list="aptos-conocidos" required value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="SUQ-101" />
          <datalist id="aptos-conocidos">
            {conocidos.map((id) => (
              <option key={id} value={id} />
            ))}
          </datalist>
        </label>
        <button className="boton boton--claro">Ver</button>
      </form>

      {apartamento && (
        <>
          <form className="pila novedades__form" onSubmit={registrar}>
            <label className="campo">
              <span>Descripción</span>
              <textarea required value={descripcion} onChange={(e) => setDescripcion(e.target.value)} placeholder="Qué pasó y dónde" />
            </label>
            <fieldset className="filtros__caracteristicas">
              <legend>Gravedad</legend>
              {GRAVEDADES.map((g) => (
                <button key={g.valor} type="button" className="chip chip--filtro" aria-pressed={gravedad === g.valor} onClick={() => setGravedad(g.valor)}>
                  {g.nombre}
                </button>
              ))}
            </fieldset>
            <div>
              <button className="boton boton--principal" disabled={accion.enCurso}>
                Registrar novedad en {apartamento}
              </button>
            </div>
          </form>
          {mensaje && <AvisoOk>{mensaje}</AvisoOk>}
          <AvisoError error={accion.error ?? historial.error} />
          {historial.cargando && !historial.datos && <Cargando />}
          {historial.datos && historial.datos.contenido.length === 0 && <p className="nota">Sin novedades.</p>}
          <ul className="novedades">
            {historial.datos?.contenido.map((n) => (
              <li key={n.id}>
                <span
                  className="chip"
                  style={{ '--chip': `var(--${COLOR_GRAVEDAD[n.gravedad]})`, '--chip-texto': n.gravedad === 'BAJA' ? 'var(--tinta)' : `var(--sobre-${COLOR_GRAVEDAD[n.gravedad]})` } as React.CSSProperties}
                >
                  {GRAVEDADES.find((g) => g.valor === n.gravedad)?.nombre}
                </span>
                <div>
                  <p>{n.descripcion}</p>
                  <p className="nota">
                    {fechaHora(n.fecha)}, {n.autor}
                  </p>
                </div>
              </li>
            ))}
          </ul>
          {historial.datos && historial.datos.totalPaginas > 1 && (
            <Paginacion
              pagina={historial.datos.pagina}
              totalPaginas={historial.datos.totalPaginas}
              totalElementos={historial.datos.totalElementos}
              alCambiar={setPagina}
            />
          )}
        </>
      )}
    </section>
  );
}

export function Operacion() {
  const { es } = useSesion();
  const [novedadesDe, setNovedadesDe] = useState('');
  const [conocidos, setConocidos] = useState<string[]>([]);
  const ancla = useRef<HTMLDivElement>(null);
  const agregar = (ids: string[]) => setConocidos((c) => [...new Set([...c, ...ids])].sort());
  const verNovedades = (id: string) => {
    setNovedadesDe(id);
    ancla.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };
  return (
    <main className="contenedor pagina pila-grande">
      <h1>Operación</h1>
      <PorPreparar alVerNovedades={verNovedades} alCargar={agregar} />
      {es('RECEPCIONISTA', 'ADMINISTRADOR') && <CalendarioOcupacion alVerNovedades={verNovedades} alCargar={agregar} />}
      <div ref={ancla}>
        <Novedades apartamento={novedadesDe} setApartamento={setNovedadesDe} conocidos={conocidos} />
      </div>
    </main>
  );
}

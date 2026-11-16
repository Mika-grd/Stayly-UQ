import { useEffect, useState, type FormEvent } from 'react';
import { administracion, type Periodo, type Temporada, type TramoPolitica } from '../../api/admin';
import { reservas } from '../../api/servicios';
import { AvisoError, AvisoOk, Cargando } from '../../components/Comunes';
import { fechaCorta, fechaHora, hoy, sumarDias } from '../../lib/formato';
import { useAccion, useCarga } from '../../lib/useCarga';

type Borrador = Omit<Temporada, 'id'>;
const NUEVA: Borrador = { nombre: '', periodos: [{ fechaInicio: hoy(), fechaFin: sumarDias(hoy(), 7) }], esBase: false, estanciaMinima: 1 };

/** Temporadas con nombre y rangos (7.4): no se solapan; la base cubre lo que no tiene temporada. */
function Temporadas() {
  const lista = useCarga(() => administracion.temporadas(), []);
  const [editando, setEditando] = useState<string | 'nueva' | null>(null);
  const [b, setB] = useState<Borrador>(NUEVA);
  const accion = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  const abrir = (t: Temporada | null) => {
    setOk(null);
    setEditando(t ? t.id : 'nueva');
    setB(t ? { nombre: t.nombre, periodos: t.periodos, esBase: t.esBase, estanciaMinima: t.estanciaMinima } : NUEVA);
  };

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    const cuerpo = { ...b, periodos: b.esBase ? [] : b.periodos };
    const r = await accion.ejecutar(() =>
      editando === 'nueva' ? administracion.crearTemporada(cuerpo) : administracion.actualizarTemporada(editando!, cuerpo),
    );
    if (r) {
      setOk(
        editando === 'nueva'
          ? `Temporada ${r.nombre} creada. Cada apartamento necesita tarifa para ella: hasta tenerla deja de venderse (A-5). Complete las tarifas en Apartamentos.`
          : `Temporada ${r.nombre} guardada.`,
      );
      setEditando(null);
      lista.recargar();
    }
  };

  const periodo = (i: number, cambio: Partial<Periodo>) =>
    setB({ ...b, periodos: b.periodos.map((p, j) => (j === i ? { ...p, ...cambio } : p)) });

  return (
    <section className="pila">
      <div className="titulo-con-accion">
        <h2>Temporadas</h2>
        <button className="boton boton--principal" onClick={() => abrir(null)}>Nueva temporada</button>
      </div>
      {ok && <AvisoOk>{ok}</AvisoOk>}
      <AvisoError error={lista.error} />
      {lista.cargando && !lista.datos && <Cargando />}
      <ul className="temporadas">
        {lista.datos?.map((t) => (
          <li key={t.id} className="placa placa--chica">
            <div>
              <h3>{t.nombre}</h3>
              <p className="nota">
                {t.esBase
                  ? 'Base: cubre las fechas sin temporada'
                  : t.periodos.map((p) => `${fechaCorta(p.fechaInicio)} al ${fechaCorta(p.fechaFin)}`).join('; ')}
                {t.estanciaMinima > 1 ? `. Mínimo ${t.estanciaMinima} noches` : ''}
              </p>
            </div>
            <button className="boton boton--claro" onClick={() => abrir(t)}>Editar</button>
          </li>
        ))}
      </ul>

      {editando && (
        <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--cafetal)' } as React.CSSProperties}>
          <h3>{editando === 'nueva' ? 'Nueva temporada' : `Editar ${b.nombre}`}</h3>
          <div className="fila">
            <label className="campo">
              <span>Nombre</span>
              <input required value={b.nombre} onChange={(e) => setB({ ...b, nombre: e.target.value })} />
            </label>
            <label className="campo">
              <span>Estancia mínima (noches)</span>
              <input required type="number" min={1} value={b.estanciaMinima} onChange={(e) => setB({ ...b, estanciaMinima: Number(e.target.value) })} />
            </label>
            <label className="campo campo--casilla">
              <input type="checkbox" checked={b.esBase} onChange={(e) => setB({ ...b, esBase: e.target.checked })} />
              <span>Temporada base</span>
            </label>
          </div>
          {!b.esBase && (
            <fieldset className="grupo-campos">
              <legend>Periodos</legend>
              {b.periodos.map((p, i) => (
                <div className="fila fila--acompanante" key={i}>
                  <label className="campo">
                    <span>Desde</span>
                    <input type="date" required value={p.fechaInicio} onChange={(e) => periodo(i, { fechaInicio: e.target.value })} />
                  </label>
                  <label className="campo">
                    <span>Hasta (sin incluir)</span>
                    <input type="date" required min={sumarDias(p.fechaInicio, 1)} value={p.fechaFin} onChange={(e) => periodo(i, { fechaFin: e.target.value })} />
                  </label>
                  {b.periodos.length > 1 && (
                    <button type="button" className="boton boton--texto" onClick={() => setB({ ...b, periodos: b.periodos.filter((_, j) => j !== i) })}>
                      Quitar
                    </button>
                  )}
                </div>
              ))}
              <div>
                <button type="button" className="boton boton--claro"
                  onClick={() => setB({ ...b, periodos: [...b.periodos, { fechaInicio: hoy(), fechaFin: sumarDias(hoy(), 7) }] })}>
                  Agregar periodo
                </button>
              </div>
            </fieldset>
          )}
          <AvisoError error={accion.error} />
          <div className="acciones acciones--fila">
            <button className="boton boton--principal" disabled={accion.enCurso}>Guardar</button>
            <button type="button" className="boton boton--claro" onClick={() => setEditando(null)}>Cancelar</button>
          </div>
        </form>
      )}
    </section>
  );
}

/** Política de cancelación (7.4): una vigente; cada cambio crea una versión nueva y las reservas conservan la suya. */
function Politica() {
  const vigente = useCarga(() => reservas.politicaVigente(), []);
  const [tramos, setTramos] = useState<TramoPolitica[]>([]);
  const [noShow, setNoShow] = useState(0);
  const accion = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  useEffect(() => {
    if (!vigente.datos) return;
    setTramos([...vigente.datos.tramos].sort((x, y) => y.antelacionMinimaDias - x.antelacionMinimaDias));
    setNoShow(vigente.datos.porcentajeRetencionNoShow);
  }, [vigente.datos]);

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    setOk(null);
    const r = await accion.ejecutar(() => administracion.nuevaPolitica(tramos, noShow));
    if (r) {
      setOk(`Versión ${r.version} vigente desde ${fechaHora(r.vigenteDesde)}. Las reservas ya creadas conservan su versión.`);
      vigente.recargar();
    }
  };

  const tramo = (i: number, cambio: Partial<TramoPolitica>) => setTramos(tramos.map((t, j) => (j === i ? { ...t, ...cambio } : t)));

  return (
    <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--lila)' } as React.CSSProperties}>
      <h2>Política de cancelación {vigente.datos && <span className="nota">versión {vigente.datos.version}</span>}</h2>
      {vigente.cargando && !vigente.datos && <Cargando />}
      {tramos.map((t, i) => (
        <div className="fila fila--acompanante" key={i}>
          <label className="campo">
            <span>Con al menos (días de antelación)</span>
            <input type="number" required min={0} value={t.antelacionMinimaDias} onChange={(e) => tramo(i, { antelacionMinimaDias: Number(e.target.value) })} />
          </label>
          <label className="campo">
            <span>Se retiene (%)</span>
            <input type="number" required min={0} max={100} value={t.porcentajeRetencion} onChange={(e) => tramo(i, { porcentajeRetencion: Number(e.target.value) })} />
          </label>
          {tramos.length > 2 && (
            <button type="button" className="boton boton--texto" onClick={() => setTramos(tramos.filter((_, j) => j !== i))}>Quitar</button>
          )}
        </div>
      ))}
      <div className="fila">
        <label className="campo">
          <span>No-show: se retiene (%)</span>
          <input type="number" required min={0} max={100} value={noShow} onChange={(e) => setNoShow(Number(e.target.value))} />
        </label>
      </div>
      {ok && <AvisoOk>{ok}</AvisoOk>}
      <AvisoError error={accion.error ?? vigente.error} />
      <div className="acciones acciones--fila">
        <button type="button" className="boton boton--claro" onClick={() => setTramos([...tramos, { antelacionMinimaDias: 0, porcentajeRetencion: 100 }])}>
          Agregar tramo
        </button>
        <button className="boton boton--principal" disabled={accion.enCurso}>Publicar nueva versión</button>
      </div>
    </form>
  );
}

export function AdminTemporadas() {
  return (
    <div className="pila-grande">
      <Temporadas />
      <Politica />
    </div>
  );
}

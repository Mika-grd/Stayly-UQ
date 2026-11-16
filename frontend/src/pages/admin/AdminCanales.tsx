import { useState, type FormEvent } from 'react';
import { administracion, type Canal, type ConflictoCanal, type TipoEventoCanal } from '../../api/admin';
import type { CanalOrigen } from '../../api/tipos';
import { AvisoError, AvisoOk, Cargando, Paginacion } from '../../components/Comunes';
import { fechaHora, nombreCanal, rango } from '../../lib/formato';
import { useAccion, useCarga } from '../../lib/useCarga';

const NOMBRE_EVENTO: Record<TipoEventoCanal, string> = {
  DISPONIBILIDAD_CONSULTADA: 'Consulta de disponibilidad',
  RESERVA_RECIBIDA: 'Reserva recibida',
  RESERVA_DUPLICADA: 'Mensaje repetido',
  CONFLICTO_REGISTRADO: 'Conflicto',
  CANCELACION_RECIBIDA: 'Cancelación',
};

/** Canales de venta con su credencial (7.8): la credencial se muestra una sola vez. */
function Canales() {
  const lista = useCarga(() => administracion.canales(), []);
  const [nombre, setNombre] = useState('');
  const [tipo, setTipo] = useState<CanalOrigen>('EXTERNO');
  const [nuevo, setNuevo] = useState<Canal | null>(null);
  const accion = useAccion();

  const registrar = async (e: FormEvent) => {
    e.preventDefault();
    const c = await accion.ejecutar(() => administracion.registrarCanal(nombre, tipo));
    if (c) {
      setNuevo(c);
      setNombre('');
      lista.recargar();
    }
  };

  return (
    <section className="placa pila" style={{ '--losa': 'var(--aguamarina)' } as React.CSSProperties}>
      <h2>Canales</h2>
      <AvisoError error={lista.error} />
      {lista.cargando && !lista.datos && <Cargando />}
      <ul className="movimientos">
        {lista.datos?.map((c) => (
          <li key={c.id} className="canal">
            <strong>{c.nombre}</strong>
            <span className="chip">{nombreCanal[c.tipo]}</span>
            <span className="nota">{c.activo ? 'Activo' : 'Inactivo'}</span>
            <code className="nota">{c.id}</code>
          </li>
        ))}
      </ul>
      {nuevo?.credencialGenerada && (
        <div className="aviso aviso--info">
          <strong>Credencial de {nuevo.nombre}: cópiela ahora, no se vuelve a mostrar.</strong>
          <code className="credencial">{nuevo.credencialGenerada}</code>
          El canal la envía en el encabezado X-API-Key.
        </div>
      )}
      <form className="barra" onSubmit={registrar}>
        <label className="campo campo--ancho">
          <span>Nombre del canal</span>
          <input required value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Portal de reservas aliado" />
        </label>
        <label className="campo">
          <span>Tipo</span>
          <select value={tipo} onChange={(e) => setTipo(e.target.value as CanalOrigen)}>
            {(Object.keys(nombreCanal) as CanalOrigen[]).map((t) => <option key={t} value={t}>{nombreCanal[t]}</option>)}
          </select>
        </label>
        <button className="boton boton--principal" disabled={accion.enCurso}>Registrar canal</button>
      </form>
      <AvisoError error={accion.error} />
    </section>
  );
}

/** Conflictos de reservas externas (RN-18): se revisan y se resuelven con una nota; nunca sobrescriben. */
function Conflictos() {
  const [estado, setEstado] = useState<'' | 'PENDIENTE' | 'RESUELTO'>('PENDIENTE');
  const [pagina, setPagina] = useState(0);
  const lista = useCarga(() => administracion.conflictos(estado, pagina), [estado, pagina]);
  const [abierto, setAbierto] = useState<string | null>(null);
  const [nota, setNota] = useState('');
  const accion = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  const resolver = async (c: ConflictoCanal) => {
    setOk(null);
    const r = await accion.ejecutar(() => administracion.resolverConflicto(c.id, nota));
    if (r) {
      setOk(`Conflicto de ${c.identificadorExterno} resuelto.`);
      setAbierto(null);
      setNota('');
      lista.recargar();
    }
  };

  return (
    <section className="placa pila" style={{ '--losa': 'var(--balcon)' } as React.CSSProperties}>
      <div className="titulo-con-accion">
        <h2>Conflictos</h2>
        <div className="pestanas pestanas--chicas" role="tablist">
          {(['PENDIENTE', 'RESUELTO', ''] as const).map((e) => (
            <button key={e || 'todos'} role="tab" aria-selected={estado === e} onClick={() => { setEstado(e); setPagina(0); }}>
              {e === 'PENDIENTE' ? 'Pendientes' : e === 'RESUELTO' ? 'Resueltos' : 'Todos'}
            </button>
          ))}
        </div>
      </div>
      {ok && <AvisoOk>{ok}</AvisoOk>}
      <AvisoError error={lista.error ?? accion.error} />
      {lista.cargando && !lista.datos && <Cargando />}
      {lista.datos && lista.datos.contenido.length === 0 && <p className="nota">Sin conflictos.</p>}
      <ul className="conflictos">
        {lista.datos?.contenido.map((c) => (
          <li key={c.id}>
            <div>
              <strong>{c.canal}: {c.identificadorExterno}</strong>
              <p className="nota">
                {c.identificacionApartamento}, {rango(c.fechaEntrada, c.fechaSalida)}. Choca con {c.codigoReservaVigente}. {fechaHora(c.fechaRegistro)}
              </p>
              {c.resolucion && <p>{c.resolucion}</p>}
            </div>
            {c.estado === 'PENDIENTE' && abierto !== c.id && (
              <button className="boton boton--claro" onClick={() => { setAbierto(c.id); setNota(''); }}>Resolver</button>
            )}
            {abierto === c.id && (
              <form className="pila" onSubmit={(e) => { e.preventDefault(); resolver(c); }}>
                <label className="campo">
                  <span>Resolución</span>
                  <textarea required value={nota} onChange={(e) => setNota(e.target.value)} placeholder="Se contactó al canal y se reubicó al huésped" />
                </label>
                <div className="acciones acciones--fila">
                  <button className="boton boton--principal" disabled={accion.enCurso}>Marcar resuelto</button>
                  <button type="button" className="boton boton--claro" onClick={() => setAbierto(null)}>Cancelar</button>
                </div>
              </form>
            )}
          </li>
        ))}
      </ul>
      {lista.datos && lista.datos.totalPaginas > 1 && (
        <Paginacion pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} totalElementos={lista.datos.totalElementos} alCambiar={setPagina} />
      )}
    </section>
  );
}

/** Bitácora de eventos intercambiados con los canales (7.8). */
function Bitacora() {
  const canales = useCarga(() => administracion.canales(), []);
  const [canal, setCanal] = useState('');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [pagina, setPagina] = useState(0);
  const lista = useCarga(() => administracion.bitacora({ canal: canal || undefined, desde: desde || undefined, hasta: hasta || undefined, pagina }), [canal, desde, hasta, pagina]);

  return (
    <section className="pila">
      <h2>Bitácora</h2>
      <div className="barra">
        <label className="campo">
          <span>Canal</span>
          <select value={canal} onChange={(e) => { setCanal(e.target.value); setPagina(0); }}>
            <option value="">Todos</option>
            {canales.datos?.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
        </label>
        <label className="campo">
          <span>Desde</span>
          <input type="date" value={desde} onChange={(e) => { setDesde(e.target.value); setPagina(0); }} />
        </label>
        <label className="campo">
          <span>Hasta</span>
          <input type="date" value={hasta} onChange={(e) => { setHasta(e.target.value); setPagina(0); }} />
        </label>
      </div>
      <AvisoError error={lista.error} />
      {lista.cargando && !lista.datos && <Cargando />}
      {lista.datos && (
        <div className="placa placa--plana tabla-envoltura">
          <table className="tabla tabla--filas">
            <thead>
              <tr><th>Fecha</th><th>Canal</th><th>Evento</th><th>Identificador</th><th>Resultado</th></tr>
            </thead>
            <tbody>
              {lista.datos.contenido.map((e) => (
                <tr key={e.id}>
                  <td>{fechaHora(e.fecha)}</td>
                  <td>{e.canal}</td>
                  <td>{NOMBRE_EVENTO[e.tipo]}</td>
                  <td>{e.identificadorExterno ?? '—'}</td>
                  <td>{e.resultado}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {lista.datos.contenido.length === 0 && <p className="vacio">Sin eventos.</p>}
        </div>
      )}
      {lista.datos && lista.datos.totalPaginas > 1 && (
        <Paginacion pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} totalElementos={lista.datos.totalElementos} alCambiar={setPagina} />
      )}
    </section>
  );
}

export function AdminCanales() {
  return (
    <div className="pila-grande">
      <div className="tablero">
        <Canales />
        <Conflictos />
      </div>
      <Bitacora />
    </div>
  );
}

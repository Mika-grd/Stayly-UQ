import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { operacion, reservas } from '../api/servicios';
import type { CanalOrigen, EstadoReserva, ReservaResumen } from '../api/tipos';
import { AvisoError, Cargando, EstadoReservaChip, Paginacion } from '../components/Comunes';
import { colorDe, textoSobre, variable } from '../lib/colores';
import { dinero, fechaLarga, hoy, nombreCanal, nombreEstado, rango, sumarDias } from '../lib/formato';
import { useCarga } from '../lib/useCarga';

function Movimiento({ reserva }: { reserva: ReservaResumen }) {
  return (
    <li>
      <Link to={`/reservas/${reserva.codigo}`} className="movimiento">
        <span className="movimiento__apto" style={{ background: variable(colorDe(reserva.identificacionApartamento)), color: textoSobre(colorDe(reserva.identificacionApartamento)) }}>
          {reserva.identificacionApartamento.replace('SUQ-', '')}
        </span>
        <span className="movimiento__texto">
          <strong>{reserva.nombreTitular}</strong>
          <span>{reserva.codigo}</span>
        </span>
        <EstadoReservaChip estado={reserva.estado} />
      </Link>
    </li>
  );
}

/** Resumen del asistente de IA (10.2): si no está disponible lo dice y las listas siguen igual. */
function ResumenIA({ fecha }: { fecha: string }) {
  const { datos, cargando } = useCarga(() => operacion.resumenDelDia(fecha), [fecha]);
  if (cargando && !datos) return <p className="resumen-ia resumen-ia--cargando nota">Preparando el resumen del día…</p>;
  if (!datos?.disponible) {
    return <p className="resumen-ia resumen-ia--apagado nota">Resumen automático no disponible. Las llegadas y salidas están abajo.</p>;
  }
  return (
    <aside className="resumen-ia" aria-label="Resumen del día">
      <p>{datos.texto}</p>
      <p className="nota">Redactado por IA ({datos.proveedor}). Verifique con las listas.</p>
    </aside>
  );
}

function Hoy() {
  const [fecha, setFecha] = useState(hoy());
  const { datos, error, cargando } = useCarga(() => reservas.movimientos(fecha), [fecha]);

  return (
    <>
      <div className="barra">
        <label className="campo">
          <span>Día</span>
          <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
        </label>
        <p className="barra__titulo">{fechaLarga(fecha)}</p>
      </div>
      <ResumenIA fecha={fecha} />
      <AvisoError error={error} />
      {cargando && <Cargando />}
      {datos && (
        <div className="tablero">
          <section className="placa" style={{ '--losa': 'var(--cafetal)' } as React.CSSProperties}>
            <h2>
              Llegan <span className="cifra">{datos.llegadas.length}</span>
            </h2>
            {datos.llegadas.length === 0 ? <p className="nota">Sin llegadas.</p> : <ul className="movimientos">{datos.llegadas.map((r) => <Movimiento key={r.codigo} reserva={r} />)}</ul>}
          </section>
          <section className="placa" style={{ '--losa': 'var(--balcon)' } as React.CSSProperties}>
            <h2>
              Salen <span className="cifra">{datos.salidas.length}</span>
            </h2>
            {datos.salidas.length === 0 ? <p className="nota">Sin salidas.</p> : <ul className="movimientos">{datos.salidas.map((r) => <Movimiento key={r.codigo} reserva={r} />)}</ul>}
          </section>
        </div>
      )}
    </>
  );
}

function Listado() {
  const navegar = useNavigate();
  const [estado, setEstado] = useState<EstadoReserva | ''>('');
  const [texto, setTexto] = useState('');
  const [titular, setTitular] = useState('');
  const [canal, setCanal] = useState<CanalOrigen | ''>('');
  const [apartamento, setApartamento] = useState('');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [pagina, setPagina] = useState(0);
  const { datos, error, cargando } = useCarga(
    () => reservas.listar({ estado, titular, canal, apartamento, desde, hasta, pagina }),
    [estado, titular, canal, apartamento, desde, hasta, pagina],
  );
  // Los apartamentos activos salen del calendario de hoy (recepción ya tiene acceso)
  const aptos = useCarga(() => operacion.calendario(hoy(), sumarDias(hoy(), 1)), []);
  const filtro = <T,>(fijar: (v: T) => void) => (v: T) => {
    setPagina(0);
    fijar(v);
  };

  return (
    <>
      <form
        className="barra"
        onSubmit={(e) => {
          e.preventDefault();
          setPagina(0);
          setTitular(texto.trim());
        }}
      >
        <label className="campo">
          <span>Estado</span>
          <select
            value={estado}
            onChange={(e) => {
              setPagina(0);
              setEstado(e.target.value as EstadoReserva | '');
            }}
          >
            <option value="">Todos</option>
            {(Object.keys(nombreEstado) as EstadoReserva[]).map((e) => (
              <option key={e} value={e}>
                {nombreEstado[e]}
              </option>
            ))}
          </select>
        </label>
        <label className="campo">
          <span>Canal</span>
          <select value={canal} onChange={(e) => filtro(setCanal)(e.target.value as CanalOrigen | '')}>
            <option value="">Todos</option>
            {(Object.keys(nombreCanal) as CanalOrigen[]).map((c) => (
              <option key={c} value={c}>
                {nombreCanal[c]}
              </option>
            ))}
          </select>
        </label>
        <label className="campo">
          <span>Apartamento</span>
          <select value={apartamento} onChange={(e) => filtro(setApartamento)(e.target.value)}>
            <option value="">Todos</option>
            {aptos.datos?.apartamentos.map((a) => (
              <option key={a.identificacion} value={a.identificacion}>
                {a.identificacion}
              </option>
            ))}
          </select>
        </label>
        <label className="campo">
          <span>Entrada desde</span>
          <input type="date" value={desde} onChange={(e) => filtro(setDesde)(e.target.value)} />
        </label>
        <label className="campo">
          <span>Entrada hasta</span>
          <input type="date" value={hasta} min={desde || undefined} onChange={(e) => filtro(setHasta)(e.target.value)} />
        </label>
        <label className="campo campo--ancho">
          <span>Titular</span>
          <input value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Nombre, documento o correo" />
        </label>
        <button className="boton">Buscar</button>
      </form>
      <AvisoError error={error} />
      {cargando && <Cargando />}
      {datos && (
        <div className="placa placa--plana tabla-envoltura">
          <table className="tabla tabla--filas">
            <thead>
              <tr>
                <th>Reserva</th>
                <th>Titular</th>
                <th>Apartamento</th>
                <th>Fechas</th>
                <th>Canal</th>
                <th>Estado</th>
                <th className="num">Total</th>
              </tr>
            </thead>
            <tbody>
              {datos.contenido.map((r) => (
                <tr key={r.codigo} onClick={() => navegar(`/reservas/${r.codigo}`)}>
                  <td>
                    <Link to={`/reservas/${r.codigo}`}>{r.codigo}</Link>
                  </td>
                  <td>{r.nombreTitular}</td>
                  <td>{r.identificacionApartamento}</td>
                  <td>{rango(r.fechaEntrada, r.fechaSalida)}</td>
                  <td>{nombreCanal[r.canal]}</td>
                  <td>
                    <EstadoReservaChip estado={r.estado} />
                  </td>
                  <td className="num">{dinero(r.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {datos.contenido.length === 0 && <p className="vacio">Sin resultados.</p>}
        </div>
      )}
      {datos && <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} totalElementos={datos.totalElementos} alCambiar={setPagina} />}
    </>
  );
}

export function Recepcion() {
  const [pestana, setPestana] = useState<'hoy' | 'reservas'>('hoy');
  return (
    <main className="contenedor pagina">
      <div className="titulo-con-accion">
        <h1>Recepción</h1>
        <Link to="/" className="boton boton--principal">
          Nueva reserva directa
        </Link>
      </div>
      <div className="pestanas" role="tablist">
        <button role="tab" aria-selected={pestana === 'hoy'} onClick={() => setPestana('hoy')}>
          Llegadas y salidas
        </button>
        <button role="tab" aria-selected={pestana === 'reservas'} onClick={() => setPestana('reservas')}>
          Todas las reservas
        </button>
      </div>
      {pestana === 'hoy' ? <Hoy /> : <Listado />}
    </main>
  );
}

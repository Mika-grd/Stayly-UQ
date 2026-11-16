import { useState } from 'react';
import { Link } from 'react-router-dom';
import { apartamentos, operacion } from '../api/servicios';
import type { ApartamentoDetalle } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { AvisoError, AvisoOk, Cargando, EstadoOperativoChip } from '../components/Comunes';
import { colorDe, textoSobre, variable } from '../lib/colores';
import { aFecha, hoy, sumarDias } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';

function PorPreparar() {
  const { es } = useSesion();
  const puedePreparar = es('PERSONAL_SERVICIO', 'ADMINISTRADOR');
  const carga = useCarga(() => apartamentos.porPreparar(), []);
  const accion = useAccion();
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
          </li>
        ))}
      </ul>
    </section>
  );
}

function FilaApartamento({ id, alCambiar }: { id: string; alCambiar: () => void }) {
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

function CalendarioOcupacion() {
  const [desde, setDesde] = useState(hoy());
  const hasta = sumarDias(desde, DIAS);
  const [version, setVersion] = useState(0);
  const { datos, error, cargando } = useCarga(() => operacion.calendario(desde, hasta), [desde, version]);

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
              <FilaApartamento key={a.identificacion} id={a.identificacion} alCambiar={() => setVersion((v) => v + 1)} />
            ))}
          </ul>
        </section>
      )}
    </>
  );
}

export function Operacion() {
  const { es } = useSesion();
  return (
    <main className="contenedor pagina pila-grande">
      <h1>Operación</h1>
      <PorPreparar />
      {es('RECEPCIONISTA', 'ADMINISTRADOR') && <CalendarioOcupacion />}
    </main>
  );
}

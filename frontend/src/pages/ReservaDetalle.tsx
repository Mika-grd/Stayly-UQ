import { useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { reservas } from '../api/servicios';
import type { EstadoReserva, Liquidacion, Reserva } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { AvisoError, AvisoOk, Cargando, EstadoReservaChip } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { FolioPanel } from '../components/FolioPanel';
import { colorDe, variable } from '../lib/colores';
import { dinero, fechaHora, fechaLarga, hora, nombreCanal } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';

const RECORRIDO: { estado: EstadoReserva; nombre: string }[] = [
  { estado: 'PENDIENTE', nombre: 'Creada' },
  { estado: 'CONFIRMADA', nombre: 'Confirmada' },
  { estado: 'EN_CURSO', nombre: 'Huéspedes en casa' },
  { estado: 'FINALIZADA', nombre: 'Salida registrada' },
];

function Recorrido({ estado }: { estado: EstadoReserva }) {
  if (estado === 'CANCELADA' || estado === 'NO_SHOW') {
    return (
      <div className="aviso aviso--error">
        <strong>{estado === 'CANCELADA' ? 'Reserva cancelada' : 'El huésped no llegó'}</strong>
        Las noches quedaron libres. El folio muestra lo retenido según la política.
      </div>
    );
  }
  const actual = RECORRIDO.findIndex((p) => p.estado === estado);
  return (
    <ol className="recorrido">
      {RECORRIDO.map((paso, i) => (
        <li key={paso.estado} className={i < actual ? 'hecho' : i === actual ? 'actual' : ''} aria-current={i === actual ? 'step' : undefined}>
          <span className="recorrido__numero">{i + 1}</span>
          {paso.nombre}
        </li>
      ))}
    </ol>
  );
}

export function ReservaDetalle() {
  const { codigo = '' } = useParams();
  const [params] = useSearchParams();
  const { es } = useSesion();
  const personal = es('RECEPCIONISTA', 'ADMINISTRADOR');
  const carga = useCarga(() => reservas.detalle(codigo), [codigo]);
  const accion = useAccion();
  const [mensaje, setMensaje] = useState<string | null>(params.get('nueva') ? 'Reserva creada.' : null);
  const [liquidacion, setLiquidacion] = useState<Liquidacion | null>(null);
  const [confirmarCancelacion, setConfirmarCancelacion] = useState(false);
  const [nuevaHora, setNuevaHora] = useState('');
  const [versionFolio, setVersionFolio] = useState(0);

  const transicion = async (operacion: () => Promise<Reserva>, texto: string) => {
    setMensaje(null);
    const r = await accion.ejecutar(operacion);
    if (r) {
      carga.setDatos(r);
      setMensaje(texto);
      setVersionFolio((v) => v + 1);
    }
  };

  const liquidar = async (operacion: () => Promise<Liquidacion>, texto: string) => {
    setMensaje(null);
    const l = await accion.ejecutar(operacion);
    if (l) {
      setLiquidacion(l);
      setMensaje(texto);
      setConfirmarCancelacion(false);
      carga.recargar();
      setVersionFolio((v) => v + 1);
    }
  };

  if (carga.cargando && !carga.datos) return <main className="contenedor pagina"><Cargando /></main>;
  if (!carga.datos) return <main className="contenedor pagina"><AvisoError error={carga.error} /></main>;

  const r = carga.datos;
  const color = colorDe(r.identificacionApartamento);
  const activa = r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA';

  return (
    <main className="contenedor pagina">
      <Link to={personal ? '/recepcion' : '/viajes'} className="volver">
        {personal ? 'Volver a recepción' : 'Volver a mis viajes'}
      </Link>

      <div className="reserva__cabeza placa" style={{ '--losa': variable(color) } as React.CSSProperties}>
        <div className="reserva__miniatura">
          <Fachada identificacion={r.identificacionApartamento} />
        </div>
        <div>
          <p className="reserva__codigo">Reserva {r.codigo}</p>
          <h1>
            {fechaLarga(r.fechaEntrada)} al {fechaLarga(r.fechaSalida)}
          </h1>
          <p>
            Apartamento {r.identificacionApartamento}, {r.numeroNoches} {r.numeroNoches === 1 ? 'noche' : 'noches'},{' '}
            {r.ocupantes.length} {r.ocupantes.length === 1 ? 'persona' : 'personas'}. Entró por {nombreCanal[r.canal].toLowerCase()}.
          </p>
          <EstadoReservaChip estado={r.estado} />
        </div>
      </div>

      <Recorrido estado={r.estado} />

      {mensaje && <AvisoOk>{mensaje}</AvisoOk>}
      <AvisoError error={accion.error} />
      {liquidacion && (
        <div className="aviso aviso--info">
          <strong>Liquidación con la política versión {liquidacion.versionPolitica}</strong>
          Se retienen {dinero(liquidacion.retencion)}. {liquidacion.devolucion > 0 ? `Se devuelven ${dinero(liquidacion.devolucion)}.` : 'No hay devolución.'}
        </div>
      )}

      <div className="ficha">
        <div className="ficha__texto">
          <section className="placa placa--plana">
            <h2>Huéspedes</h2>
            <ul className="ocupantes">
              {r.ocupantes.map((o) => (
                <li key={o.id}>
                  <strong>{o.nombre}</strong>
                  <span>
                    {o.titular ? 'Titular' : 'Acompañante'}, fecha de nacimiento {fechaLarga(o.fechaNacimiento)}
                    {o.facturable ? '' : ', no paga'}
                  </span>
                </li>
              ))}
            </ul>
            <p>
              Hora estimada de llegada: <strong>{hora(r.horaEstimadaLlegada)}</strong>
              {r.mascotasAutorizadas > 0 && `. Mascotas: ${r.mascotasAutorizadas}`}
            </p>
            {r.advertenciaAcceso && (
              <div className="aviso aviso--info">
                <strong>Acceso</strong>
                {r.advertenciaAcceso}
              </div>
            )}
            {activa && (
              <form
                className="fila fila--accion"
                onSubmit={(e) => {
                  e.preventDefault();
                  transicion(() => reservas.horaLlegada(r.codigo, nuevaHora), 'Hora de llegada actualizada.');
                }}
              >
                <label className="campo">
                  <span>{r.horaEstimadaLlegada ? 'Cambiar hora de llegada' : 'Indicar hora de llegada'}</span>
                  <input type="time" required value={nuevaHora} onChange={(e) => setNuevaHora(e.target.value)} />
                </label>
                <button className="boton boton--claro" disabled={accion.enCurso}>
                  Guardar hora
                </button>
              </form>
            )}
          </section>

          <FolioPanel key={versionFolio} codigo={r.codigo} alCambiar={() => carga.recargar()} />
        </div>

        <aside className="reservar placa placa--triple" style={{ '--losa': variable(color) } as React.CSSProperties}>
          <p className="reservar__precio">
            <span className="cifra">{dinero(r.valor.total)}</span>
            <span> por el alojamiento</span>
          </p>
          <p>
            Saldo del folio: <strong className="cifra">{dinero(r.saldoFolio)}</strong>
          </p>
          <p className="nota">Creada el {fechaHora(r.fechaCreacion)}, con la política versión {r.versionPolitica}.</p>

          {personal && (
            <div className="acciones">
              {r.estado === 'PENDIENTE' && (
                <button className="boton boton--cobalto boton--ancho" disabled={accion.enCurso} onClick={() => transicion(() => reservas.confirmar(r.codigo), 'Reserva confirmada.')}>
                  Confirmar reserva
                </button>
              )}
              {r.estado === 'CONFIRMADA' && (
                <>
                  <button className="boton boton--cafetal boton--ancho" disabled={accion.enCurso} onClick={() => transicion(() => reservas.registrarLlegada(r.codigo), 'Llegada registrada.')}>
                    Registrar llegada
                  </button>
                  <button className="boton boton--claro boton--ancho" disabled={accion.enCurso} onClick={() => liquidar(() => reservas.noShow(r.codigo), 'No-show registrado.')}>
                    Declarar que no llegó
                  </button>
                </>
              )}
              {r.estado === 'EN_CURSO' && (
                <button className="boton boton--cafetal boton--ancho" disabled={accion.enCurso} onClick={() => transicion(() => reservas.registrarSalida(r.codigo), 'Salida registrada.')}>
                  Registrar salida
                </button>
              )}
            </div>
          )}

          {activa && (
            <div className="acciones">
              {!confirmarCancelacion ? (
                <button className="boton boton--claro boton--ancho" onClick={() => setConfirmarCancelacion(true)}>
                  Cancelar reserva
                </button>
              ) : (
                <div className="aviso aviso--error">
                  <strong>¿Cancelar la reserva {r.codigo}?</strong>
                  Se retiene lo que indique la política según la antelación. Esto no se puede deshacer.
                  <div className="acciones acciones--fila">
                    <button className="boton boton--principal" disabled={accion.enCurso} onClick={() => liquidar(() => reservas.cancelar(r.codigo), 'Reserva cancelada.')}>
                      Sí, cancelar
                    </button>
                    <button className="boton boton--claro" onClick={() => setConfirmarCancelacion(false)}>
                      Mantener la reserva
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </aside>
      </div>
    </main>
  );
}

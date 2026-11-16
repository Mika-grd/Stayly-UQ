import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { operacion, reservas } from '../api/servicios';
import type { DatosAcompanante, DatosTitular } from '../api/tipos';
import { AvisoError, Cargando } from '../components/Comunes';
import { Desglose } from '../components/Desglose';
import { Fachada } from '../components/Fachada';
import { colorDe, variable } from '../lib/colores';
import { dinero, hoy, noches, rango, sumarDias } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';

/**
 * Modificación de una reserva no iniciada (7.5, CU-13): fechas, ocupantes y apartamento. El API
 * repite las seis validaciones, recalcula el valor y registra la diferencia como ajuste en el folio.
 */
export function ModificarReserva() {
  const { codigo = '' } = useParams();
  const navegar = useNavigate();
  const accion = useAccion();
  const reserva = useCarga(() => reservas.detalle(codigo), [codigo]);
  const aptos = useCarga(() => operacion.calendario(hoy(), sumarDias(hoy(), 1)), []);

  const [apartamento, setApartamento] = useState('');
  const [entrada, setEntrada] = useState('');
  const [salida, setSalida] = useState('');
  const [titular, setTitular] = useState<DatosTitular>({ nombre: '', documentoIdentidad: '', fechaNacimiento: '', correo: '', telefono: '' });
  const [acompanantes, setAcompanantes] = useState<DatosAcompanante[]>([]);
  const [mascotas, setMascotas] = useState(0);

  // Precarga con los datos actuales de la reserva
  useEffect(() => {
    const r = reserva.datos;
    if (!r) return;
    const t = r.ocupantes.find((o) => o.titular);
    setApartamento(r.identificacionApartamento);
    setEntrada(r.fechaEntrada);
    setSalida(r.fechaSalida);
    setMascotas(r.mascotasAutorizadas);
    setTitular({
      nombre: t?.nombre ?? '',
      documentoIdentidad: t?.documentoIdentidad ?? '',
      fechaNacimiento: t?.fechaNacimiento ?? '',
      correo: t?.correo ?? '',
      telefono: t?.telefono ?? '',
    });
    setAcompanantes(
      r.ocupantes
        .filter((o) => !o.titular)
        .map((o) => ({ nombre: o.nombre, fechaNacimiento: o.fechaNacimiento, documentoIdentidad: o.documentoIdentidad ?? undefined })),
    );
  }, [reserva.datos]);

  const fechas = [titular.fechaNacimiento, ...acompanantes.map((a) => a.fechaNacimiento)];
  const listo = Boolean(apartamento && entrada && salida > entrada && fechas.every(Boolean));
  const cotizacion = useCarga(
    () => (listo ? reservas.cotizar({ apartamento, entrada, salida, fechasNacimiento: fechas, mascotas }) : Promise.resolve(null)),
    [listo, apartamento, entrada, salida, fechas.join(','), mascotas],
  );

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const r = await accion.ejecutar(() =>
      reservas.modificar(codigo, {
        apartamento,
        entrada,
        salida,
        titular,
        acompanantes: acompanantes.map((a) => ({ ...a, documentoIdentidad: a.documentoIdentidad || undefined })),
        mascotas,
      }),
    );
    if (r) navegar(`/reservas/${codigo}?modificada=1`);
  };

  if (reserva.cargando && !reserva.datos) return <main className="contenedor pagina"><Cargando /></main>;
  if (!reserva.datos) return <main className="contenedor pagina"><AvisoError error={reserva.error} /></main>;

  const r = reserva.datos;
  if (r.estado !== 'PENDIENTE' && r.estado !== 'CONFIRMADA') {
    return (
      <main className="contenedor pagina">
        <div className="aviso aviso--error">
          <strong>Solo se modifican reservas pendientes o confirmadas.</strong>
        </div>
        <Link to={`/reservas/${codigo}`}>Volver a la reserva</Link>
      </main>
    );
  }

  const losa = variable(colorDe(apartamento || r.identificacionApartamento));
  const totalNoches = salida > entrada ? noches(entrada, salida) : 0;
  const campoTitular = (clave: keyof DatosTitular) => ({
    value: titular[clave],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => setTitular((t) => ({ ...t, [clave]: e.target.value })),
  });
  const diferencia = cotizacion.datos ? cotizacion.datos.total - r.valor.total : null;

  return (
    <main className="contenedor pagina">
      <Link to={`/reservas/${codigo}`} className="volver">
        Volver a la reserva
      </Link>
      <h1>Modificar {codigo}</h1>

      <form className="ficha" onSubmit={enviar}>
        <div className="ficha__texto">
          <fieldset className="placa grupo-campos">
            <legend>Estancia</legend>
            <div className="fila">
              <label className="campo">
                <span>Apartamento</span>
                <select value={apartamento} onChange={(e) => setApartamento(e.target.value)}>
                  {(aptos.datos?.apartamentos.map((a) => a.identificacion) ?? [r.identificacionApartamento]).map((id) => (
                    <option key={id} value={id}>
                      {id}
                    </option>
                  ))}
                </select>
              </label>
              <label className="campo">
                <span>Llegada</span>
                <input
                  type="date"
                  required
                  min={hoy()}
                  value={entrada}
                  onChange={(e) => {
                    setEntrada(e.target.value);
                    if (salida <= e.target.value) setSalida(sumarDias(e.target.value, 1));
                  }}
                />
              </label>
              <label className="campo">
                <span>Salida</span>
                <input type="date" required min={entrada ? sumarDias(entrada, 1) : undefined} value={salida} onChange={(e) => setSalida(e.target.value)} />
              </label>
              <label className="campo">
                <span>Mascotas</span>
                <input type="number" min={0} max={5} value={mascotas} onChange={(e) => setMascotas(Number(e.target.value))} />
              </label>
            </div>
          </fieldset>

          <fieldset className="placa grupo-campos">
            <legend>Titular</legend>
            <div className="fila">
              <label className="campo">
                <span>Nombre completo</span>
                <input required {...campoTitular('nombre')} />
              </label>
              <label className="campo">
                <span>Documento de identidad</span>
                <input required {...campoTitular('documentoIdentidad')} inputMode="numeric" />
              </label>
              <label className="campo">
                <span>Fecha de nacimiento</span>
                <input required type="date" max={hoy()} {...campoTitular('fechaNacimiento')} />
              </label>
              <label className="campo">
                <span>Correo</span>
                <input required type="email" {...campoTitular('correo')} />
              </label>
              <label className="campo">
                <span>Teléfono</span>
                <input required {...campoTitular('telefono')} />
              </label>
            </div>
          </fieldset>

          <fieldset className="placa grupo-campos">
            <legend>Acompañantes</legend>
            {acompanantes.map((a, i) => (
              <div className="fila fila--acompanante" key={i}>
                <label className="campo">
                  <span>Nombre</span>
                  <input
                    required
                    value={a.nombre}
                    onChange={(e) => setAcompanantes((l) => l.map((x, j) => (j === i ? { ...x, nombre: e.target.value } : x)))}
                  />
                </label>
                <label className="campo">
                  <span>Fecha de nacimiento</span>
                  <input
                    required
                    type="date"
                    max={hoy()}
                    value={a.fechaNacimiento}
                    onChange={(e) => setAcompanantes((l) => l.map((x, j) => (j === i ? { ...x, fechaNacimiento: e.target.value } : x)))}
                  />
                </label>
                <button type="button" className="boton boton--texto" onClick={() => setAcompanantes((l) => l.filter((_, j) => j !== i))}>
                  Quitar
                </button>
              </div>
            ))}
            <button type="button" className="boton boton--claro" onClick={() => setAcompanantes((l) => [...l, { nombre: '', fechaNacimiento: '' }])}>
              Agregar acompañante
            </button>
          </fieldset>
        </div>

        <aside className="reservar placa placa--triple" style={{ '--losa': losa } as React.CSSProperties}>
          <div className="reservar__miniatura">
            <Fachada identificacion={apartamento || r.identificacionApartamento} />
          </div>
          <p>
            {entrada && salida > entrada ? rango(entrada, salida) : 'Fechas incompletas'}
            {totalNoches > 0 && `, ${totalNoches} ${totalNoches === 1 ? 'noche' : 'noches'}`}
          </p>
          {cotizacion.cargando && listo && <Cargando />}
          <AvisoError error={cotizacion.error} />
          {cotizacion.datos && (
            <>
              <dl className="cuenta">
                <dt>Valor actual</dt>
                <dd>{dinero(r.valor.total)}</dd>
                <dt>Nuevo valor</dt>
                <dd>{dinero(cotizacion.datos.total)}</dd>
                <dt className="cuenta__total">Ajuste en el folio</dt>
                <dd className="cuenta__total cifra">
                  {diferencia != null && diferencia > 0 ? '+' : ''}
                  {dinero(diferencia)}
                </dd>
              </dl>
              <Desglose noches={cotizacion.datos.noches} />
            </>
          )}
          <AvisoError error={accion.error} />
          <button type="submit" className="boton boton--principal boton--ancho" disabled={accion.enCurso || !listo}>
            {accion.enCurso ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </aside>
      </form>
    </main>
  );
}

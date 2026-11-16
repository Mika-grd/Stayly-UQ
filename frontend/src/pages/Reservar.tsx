import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { apartamentos, reservas } from '../api/servicios';
import type { DatosAcompanante, DatosTitular } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { aParametros, criteriosDe } from '../components/Busqueda';
import { AvisoError, Cargando } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { colorDe, variable } from '../lib/colores';
import { dinero, hoy, noches, rango, restarAnios } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';

export function Reservar() {
  const { id = '' } = useParams();
  const [params] = useSearchParams();
  const criterios = useMemo(() => criteriosDe(params), [params]);
  const { usuario, es } = useSesion();
  const navegar = useNavigate();
  const accion = useAccion();
  const esHuesped = es('HUESPED');

  const detalle = useCarga(() => apartamentos.detalle(id), [id]);

  const [titular, setTitular] = useState<DatosTitular>({
    nombre: '',
    documentoIdentidad: '',
    fechaNacimiento: '',
    correo: '',
    telefono: '',
  });
  const [acompanantes, setAcompanantes] = useState<DatosAcompanante[]>(() => [
    ...Array.from({ length: Math.max(criterios.adultos - 1, 0) }, () => ({ nombre: '', fechaNacimiento: restarAnios(hoy(), 30) })),
    ...Array.from({ length: criterios.ninos }, () => ({ nombre: '', fechaNacimiento: restarAnios(hoy(), 6) })),
  ]);
  const [hora, setHora] = useState('');
  const [mascotas, setMascotas] = useState(0);

  // El huésped reserva a su nombre: se completan los datos de su cuenta.
  useEffect(() => {
    if (esHuesped && usuario) {
      setTitular((t) => ({
        ...t,
        nombre: t.nombre || usuario.nombre,
        correo: t.correo || usuario.correo,
        telefono: t.telefono || usuario.telefono,
      }));
    }
  }, [esHuesped, usuario]);

  const fechas = [titular.fechaNacimiento, ...acompanantes.map((a) => a.fechaNacimiento)];
  const fechasCompletas = fechas.every(Boolean);
  const cotizacion = useCarga(
    () =>
      fechasCompletas
        ? reservas.cotizar({ apartamento: id, entrada: criterios.entrada, salida: criterios.salida, fechasNacimiento: fechas, mascotas })
        : Promise.resolve(null),
    [id, criterios.entrada, criterios.salida, fechas.join(','), mascotas],
  );

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const creada = await accion.ejecutar(() =>
      reservas.crear({
        apartamento: id,
        entrada: criterios.entrada,
        salida: criterios.salida,
        horaEstimadaLlegada: hora,
        titular,
        acompanantes: acompanantes.map((a) => ({ ...a, documentoIdentidad: a.documentoIdentidad || undefined })),
        mascotas,
      }),
    );
    if (creada) navegar(`/reservas/${creada.codigo}?nueva=1`);
  };

  if (detalle.cargando) return <main className="contenedor pagina"><Cargando /></main>;
  if (!detalle.datos) return <main className="contenedor pagina"><AvisoError error={detalle.error} /></main>;

  const apto = detalle.datos;
  const losa = variable(colorDe(apto.identificacion));
  const totalNoches = noches(criterios.entrada, criterios.salida);
  const campoTitular = (clave: keyof DatosTitular) => ({
    value: titular[clave],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => setTitular((t) => ({ ...t, [clave]: e.target.value })),
  });

  return (
    <main className="contenedor pagina">
      <Link to={`/apartamentos/${id}?${aParametros(criterios)}`} className="volver">
        Volver al apartamento
      </Link>
      <h1>Reservar {apto.nombre}</h1>
      <p>
        Del {rango(criterios.entrada, criterios.salida)}, {totalNoches} {totalNoches === 1 ? 'noche' : 'noches'}.
        {esHuesped ? ' La reserva queda a su nombre.' : ' Reserva directa desde recepción.'}
      </p>

      <form className="ficha" onSubmit={enviar}>
        <div className="ficha__texto">
          <fieldset className="placa grupo-campos">
            <legend>Titular de la reserva</legend>
            <div className="fila">
              <label className="campo">
                <span>Nombre completo</span>
                <input required {...campoTitular('nombre')} autoComplete="name" />
              </label>
              <label className="campo">
                <span>Documento de identidad</span>
                <input required {...campoTitular('documentoIdentidad')} inputMode="numeric" />
                {esHuesped && <small>El mismo con el que creó su cuenta.</small>}
              </label>
              <label className="campo">
                <span>Fecha de nacimiento</span>
                <input required type="date" max={hoy()} {...campoTitular('fechaNacimiento')} />
              </label>
              <label className="campo">
                <span>Correo</span>
                <input required type="email" {...campoTitular('correo')} autoComplete="email" />
              </label>
              <label className="campo">
                <span>Teléfono</span>
                <input required {...campoTitular('telefono')} autoComplete="tel" />
              </label>
            </div>
          </fieldset>

          <fieldset className="placa grupo-campos">
            <legend>Acompañantes</legend>
            {acompanantes.length === 0 && <p>Viaja solo. Puede agregar acompañantes si cambia el grupo.</p>}
            {acompanantes.map((a, i) => (
              <div className="fila fila--acompanante" key={i}>
                <label className="campo">
                  <span>Nombre</span>
                  <input
                    required
                    value={a.nombre}
                    onChange={(e) =>
                      setAcompanantes((lista) => lista.map((x, j) => (j === i ? { ...x, nombre: e.target.value } : x)))
                    }
                  />
                </label>
                <label className="campo">
                  <span>Fecha de nacimiento</span>
                  <input
                    required
                    type="date"
                    max={hoy()}
                    value={a.fechaNacimiento}
                    onChange={(e) =>
                      setAcompanantes((lista) =>
                        lista.map((x, j) => (j === i ? { ...x, fechaNacimiento: e.target.value } : x)),
                      )
                    }
                  />
                </label>
                <button
                  type="button"
                  className="boton boton--texto"
                  onClick={() => setAcompanantes((lista) => lista.filter((_, j) => j !== i))}
                >
                  Quitar
                </button>
              </div>
            ))}
            {acompanantes.length + 1 < apto.capacidad && (
              <button
                type="button"
                className="boton boton--claro"
                onClick={() => setAcompanantes((lista) => [...lista, { nombre: '', fechaNacimiento: '' }])}
              >
                Agregar acompañante
              </button>
            )}
          </fieldset>

          <fieldset className="placa grupo-campos">
            <legend>Llegada</legend>
            <div className="fila">
              <label className="campo">
                <span>Hora estimada de llegada</span>
                <input type="time" value={hora} onChange={(e) => setHora(e.target.value)} />
                <small>Se necesita para confirmar la reserva. Puede indicarla después.</small>
              </label>
              {apto.admiteMascotas && (
                <label className="campo">
                  <span>Mascotas</span>
                  <input type="number" min={0} max={5} value={mascotas} onChange={(e) => setMascotas(Number(e.target.value))} />
                </label>
              )}
            </div>
          </fieldset>
        </div>

        <aside className="reservar placa placa--triple" style={{ '--losa': losa } as React.CSSProperties}>
          <div className="reservar__miniatura">
            <Fachada identificacion={apto.identificacion} dormitorios={apto.dormitorios} />
          </div>
          <h3>{apto.nombre}</h3>
          <p>{rango(criterios.entrada, criterios.salida)}</p>

          {!fechasCompletas && <p className="nota">Complete las fechas de nacimiento para ver el total.</p>}
          {cotizacion.cargando && fechasCompletas && <Cargando texto="Calculando…" />}
          <AvisoError error={cotizacion.error} />
          {cotizacion.datos && (
            <dl className="cuenta">
              <dt>Alojamiento</dt>
              <dd>{dinero(cotizacion.datos.valorAlojamiento)}</dd>
              {cotizacion.datos.cargoMascotas ? (
                <>
                  <dt>Mascotas</dt>
                  <dd>{dinero(cotizacion.datos.cargoMascotas)}</dd>
                </>
              ) : null}
              <dt>Personas que pagan</dt>
              <dd>
                {cotizacion.datos.ocupantesFacturables} de {cotizacion.datos.ocupantesTotales}
              </dd>
              <dt className="cuenta__total">Total</dt>
              <dd className="cuenta__total cifra">{dinero(cotizacion.datos.total)}</dd>
            </dl>
          )}

          <AvisoError error={accion.error} />
          <button type="submit" className="boton boton--principal boton--ancho" disabled={accion.enCurso}>
            {accion.enCurso ? 'Reservando…' : 'Reservar'}
          </button>
          <p className="nota">La reserva queda pendiente hasta que se pague el anticipo.</p>
        </aside>
      </form>
    </main>
  );
}

import { useEffect, useState, type FormEvent } from 'react';
import { administracion, type Alojamiento, type ReglasPropias } from '../../api/admin';
import type { MedioPago } from '../../api/tipos';
import { AvisoError, AvisoOk, Cargando } from '../../components/Comunes';
import { nombreMedio } from '../../lib/formato';
import { useAccion, useCarga } from '../../lib/useCarga';

const hhmm = (v: string) => v.slice(0, 5);

/** Datos, horarios, normas, servicios y parámetros del alojamiento (7.3, sección 6) y reglas propias (CU-37). */
export function AdminAlojamiento() {
  const carga = useCarga(() => administracion.alojamiento(), []);
  const [a, setA] = useState<Alojamiento | null>(null);
  const [reglas, setReglas] = useState<ReglasPropias | null>(null);
  const datos = useAccion();
  const propias = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  useEffect(() => {
    if (!carga.datos) return;
    const d = carga.datos;
    setA({
      ...d,
      horaEntrada: hhmm(d.horaEntrada),
      horaSalida: hhmm(d.horaSalida),
      parametros: { ...d.parametros, horaLimiteNoShow: hhmm(d.parametros.horaLimiteNoShow) },
    });
    if (d.reglasPropias) setReglas({ ...d.reglasPropias, horaInicioLlegadaNocturna: hhmm(d.reglasPropias.horaInicioLlegadaNocturna) });
  }, [carga.datos]);

  if (carga.cargando && !a) return <Cargando />;
  if (!a) return <AvisoError error={carga.error} />;

  const cambiar = <K extends keyof Alojamiento>(k: K, v: Alojamiento[K]) => setA({ ...a, [k]: v });
  const param = <K extends keyof Alojamiento['parametros']>(k: K, v: Alojamiento['parametros'][K]) =>
    setA({ ...a, parametros: { ...a.parametros, [k]: v } });

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    setOk(null);
    const { reglasPropias: _, ...cuerpo } = a;
    const r = await datos.ejecutar(() =>
      administracion.guardarAlojamiento({
        ...cuerpo,
        normasConvivencia: cuerpo.normasConvivencia.filter((n) => n.trim()),
        serviciosAdicionales: cuerpo.serviciosAdicionales.filter((s) => s.nombre.trim()),
        parametros: { ...cuerpo.parametros, porcentajeAnticipo: cuerpo.parametros.exigeAnticipo ? cuerpo.parametros.porcentajeAnticipo : null },
      }),
    );
    if (r) setOk('Alojamiento guardado. Rige desde la siguiente operación.');
  };

  const guardarReglas = async (e: FormEvent) => {
    e.preventDefault();
    setOk(null);
    if (!reglas) return;
    const r = await propias.ejecutar(() => administracion.guardarReglasPropias(reglas));
    if (r) setOk('Reglas propias guardadas.');
  };

  return (
    <div className="pila-grande">
      {ok && <AvisoOk>{ok}</AvisoOk>}
      <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
        <h2>Datos y ubicación</h2>
        <div className="fila">
          <label className="campo">
            <span>Nombre</span>
            <input required value={a.nombre} onChange={(e) => cambiar('nombre', e.target.value)} />
          </label>
          <label className="campo">
            <span>Ciudad</span>
            <input required value={a.ciudad} onChange={(e) => cambiar('ciudad', e.target.value)} />
          </label>
          <label className="campo">
            <span>Dirección</span>
            <input required value={a.direccion} onChange={(e) => cambiar('direccion', e.target.value)} />
          </label>
          <label className="campo">
            <span>Latitud</span>
            <input required type="number" step="0.0001" min={-90} max={90} value={a.ubicacion.latitud}
              onChange={(e) => cambiar('ubicacion', { ...a.ubicacion, latitud: Number(e.target.value) })} />
          </label>
          <label className="campo">
            <span>Longitud</span>
            <input required type="number" step="0.0001" min={-180} max={180} value={a.ubicacion.longitud}
              onChange={(e) => cambiar('ubicacion', { ...a.ubicacion, longitud: Number(e.target.value) })} />
          </label>
        </div>
        <label className="campo">
          <span>Descripción</span>
          <textarea required value={a.descripcion} onChange={(e) => cambiar('descripcion', e.target.value)} />
        </label>

        <h3>Horarios</h3>
        <div className="fila">
          <label className="campo">
            <span>Hora de entrada</span>
            <input required type="time" value={a.horaEntrada} onChange={(e) => cambiar('horaEntrada', e.target.value)} />
          </label>
          <label className="campo">
            <span>Hora de salida</span>
            <input required type="time" value={a.horaSalida} onChange={(e) => cambiar('horaSalida', e.target.value)} />
          </label>
        </div>

        <h3>Parámetros</h3>
        <div className="fila">
          <label className="campo">
            <span>Edad facturable (años)</span>
            <input required type="number" min={0} value={a.parametros.umbralEdadFacturable}
              onChange={(e) => param('umbralEdadFacturable', Number(e.target.value))} />
          </label>
          <label className="campo">
            <span>Preparación (horas)</span>
            <input required type="number" min={0} value={a.parametros.tiempoPreparacionHoras}
              onChange={(e) => param('tiempoPreparacionHoras', Number(e.target.value))} />
          </label>
          <label className="campo">
            <span>Plazo de confirmación (horas)</span>
            <input required type="number" min={1} value={a.parametros.plazoConfirmacionHoras}
              onChange={(e) => param('plazoConfirmacionHoras', Number(e.target.value))} />
          </label>
          <label className="campo">
            <span>Hora límite de no-show</span>
            <input required type="time" value={a.parametros.horaLimiteNoShow} onChange={(e) => param('horaLimiteNoShow', e.target.value)} />
          </label>
          <label className="campo campo--casilla">
            <input type="checkbox" checked={a.parametros.exigeAnticipo} onChange={(e) => param('exigeAnticipo', e.target.checked)} />
            <span>Exige anticipo</span>
          </label>
          {a.parametros.exigeAnticipo && (
            <label className="campo">
              <span>Anticipo (%)</span>
              <input required type="number" min={1} max={100} value={a.parametros.porcentajeAnticipo ?? ''}
                onChange={(e) => param('porcentajeAnticipo', Number(e.target.value))} />
            </label>
          )}
        </div>
        <fieldset className="filtros__caracteristicas">
          <legend>Medios de pago aceptados</legend>
          {(Object.keys(nombreMedio) as MedioPago[]).map((m) => (
            <button key={m} type="button" className="chip chip--filtro" aria-pressed={a.parametros.mediosPagoAceptados.includes(m)}
              onClick={() =>
                param('mediosPagoAceptados', a.parametros.mediosPagoAceptados.includes(m)
                  ? a.parametros.mediosPagoAceptados.filter((x) => x !== m)
                  : [...a.parametros.mediosPagoAceptados, m])}>
              {nombreMedio[m]}
            </button>
          ))}
        </fieldset>

        <h3>Normas de convivencia</h3>
        <ul className="lista-editable">
          {a.normasConvivencia.map((n, i) => (
            <li key={i}>
              <input value={n} aria-label={`Norma ${i + 1}`}
                onChange={(e) => cambiar('normasConvivencia', a.normasConvivencia.map((x, j) => (j === i ? e.target.value : x)))} />
              <button type="button" className="boton boton--texto"
                onClick={() => cambiar('normasConvivencia', a.normasConvivencia.filter((_, j) => j !== i))}>Quitar</button>
            </li>
          ))}
        </ul>
        <div>
          <button type="button" className="boton boton--claro" onClick={() => cambiar('normasConvivencia', [...a.normasConvivencia, ''])}>
            Agregar norma
          </button>
        </div>

        <h3>Servicios adicionales</h3>
        <ul className="lista-editable">
          {a.serviciosAdicionales.map((s, i) => (
            <li key={i}>
              <input value={s.nombre} aria-label={`Servicio ${i + 1}`}
                onChange={(e) => cambiar('serviciosAdicionales', a.serviciosAdicionales.map((x, j) => (j === i ? { ...x, nombre: e.target.value } : x)))} />
              <label className="campo--casilla">
                <input type="checkbox" checked={s.generaCargo}
                  onChange={(e) => cambiar('serviciosAdicionales', a.serviciosAdicionales.map((x, j) => (j === i ? { ...x, generaCargo: e.target.checked, valor: e.target.checked ? x.valor ?? 0 : null } : x)))} />
                <span>Cobra</span>
              </label>
              {s.generaCargo && (
                <input type="number" min={0} step={1000} value={s.valor ?? 0} aria-label="Valor"
                  onChange={(e) => cambiar('serviciosAdicionales', a.serviciosAdicionales.map((x, j) => (j === i ? { ...x, valor: Number(e.target.value) } : x)))} />
              )}
              <button type="button" className="boton boton--texto"
                onClick={() => cambiar('serviciosAdicionales', a.serviciosAdicionales.filter((_, j) => j !== i))}>Quitar</button>
            </li>
          ))}
        </ul>
        <div>
          <button type="button" className="boton boton--claro"
            onClick={() => cambiar('serviciosAdicionales', [...a.serviciosAdicionales, { nombre: '', generaCargo: false, valor: null }])}>
            Agregar servicio
          </button>
        </div>

        <AvisoError error={datos.error} />
        <div>
          <button className="boton boton--principal" disabled={datos.enCurso}>Guardar alojamiento</button>
        </div>
      </form>

      {reglas && (
        <form className="placa pila" onSubmit={guardarReglas} style={{ '--losa': 'var(--lila)' } as React.CSSProperties}>
          <h2>Reglas propias</h2>
          <div className="fila">
            <label className="campo">
              <span>Cupo de mascotas (RP-02)</span>
              <input required type="number" min={0} value={reglas.cupoMascotas} onChange={(e) => setReglas({ ...reglas, cupoMascotas: Number(e.target.value) })} />
            </label>
            <label className="campo">
              <span>Cargo por mascota y noche</span>
              <input required type="number" min={0} step={1000} value={reglas.cargoPorMascotaNoche}
                onChange={(e) => setReglas({ ...reglas, cargoPorMascotaNoche: Number(e.target.value) })} />
            </label>
            <label className="campo">
              <span>Llegada nocturna desde (RP-03)</span>
              <input required type="time" value={reglas.horaInicioLlegadaNocturna}
                onChange={(e) => setReglas({ ...reglas, horaInicioLlegadaNocturna: e.target.value })} />
            </label>
            <label className="campo">
              <span>Recargo por llegada nocturna</span>
              <input required type="number" min={0} step={1000} value={reglas.valorRecargoLlegadaNocturna}
                onChange={(e) => setReglas({ ...reglas, valorRecargoLlegadaNocturna: Number(e.target.value) })} />
            </label>
          </div>
          <AvisoError error={propias.error} />
          <div>
            <button className="boton" disabled={propias.enCurso}>Guardar reglas propias</button>
          </div>
        </form>
      )}
    </div>
  );
}

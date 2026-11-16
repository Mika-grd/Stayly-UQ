import { useEffect, useState, type FormEvent } from 'react';
import { administracion, type ApartamentoDatos } from '../../api/admin';
import { apartamentos } from '../../api/servicios';
import { AvisoError, AvisoOk, Cargando, EstadoOperativoChip, Paginacion } from '../../components/Comunes';
import { colorDe, textoSobre, variable } from '../../lib/colores';
import { dinero, fechaHora, hoy, sumarDias } from '../../lib/formato';
import { useAccion, useCarga } from '../../lib/useCarga';

const VACIO: ApartamentoDatos = {
  identificacion: '',
  nombre: '',
  descripcion: '',
  dormitorios: 1,
  capacidad: 2,
  caracteristicas: [],
  admiteMascotas: false,
  advertenciaAcceso: '',
  imagenes: [{ url: '', principal: true }],
};

/** Formulario de creación o edición (7.3): datos, características e imágenes (1 a 10, una principal). */
function EditorApartamento({ id, alGuardar }: { id: string | null; alGuardar: (id: string, aviso: string) => void }) {
  const [d, setD] = useState<ApartamentoDatos>(VACIO);
  const [caracteristicas, setCaracteristicas] = useState('');
  const accion = useAccion();
  const detalle = useCarga(() => (id ? apartamentos.detalle(id) : Promise.resolve(null)), [id]);

  useEffect(() => {
    const a = detalle.datos;
    if (!id) {
      setD(VACIO);
      setCaracteristicas('');
    } else if (a) {
      setD({
        identificacion: a.identificacion,
        nombre: a.nombre,
        descripcion: a.descripcion,
        dormitorios: a.dormitorios,
        capacidad: a.capacidad,
        caracteristicas: a.caracteristicas,
        admiteMascotas: a.admiteMascotas,
        advertenciaAcceso: a.advertenciaAcceso ?? '',
        imagenes: a.imagenes.length ? a.imagenes : VACIO.imagenes,
      });
      setCaracteristicas(a.caracteristicas.join(', '));
    }
  }, [id, detalle.datos]);

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    const cuerpo: ApartamentoDatos = {
      ...d,
      advertenciaAcceso: d.advertenciaAcceso?.trim() || null,
      caracteristicas: caracteristicas.split(',').map((c) => c.trim()).filter(Boolean),
      imagenes: d.imagenes.filter((i) => i.url.trim()),
    };
    if (id) {
      const r = await accion.ejecutar(() => administracion.actualizarApartamento(id, cuerpo));
      if (r) {
        const sobre = r.reservasSobreNuevaCapacidad;
        alGuardar(id, sobre.length ? `Guardado. Quedan por encima de la nueva capacidad: ${sobre.join(', ')}.` : 'Apartamento guardado.');
      }
    } else {
      const r = await accion.ejecutar(() => administracion.crearApartamento(cuerpo));
      if (r) alGuardar(r.identificacion, `${r.identificacion} creado. Queda inactivo hasta tener tarifas completas.`);
    }
  };

  const imagen = (i: number, cambio: Partial<{ url: string; principal: boolean }>) =>
    setD({
      ...d,
      imagenes: d.imagenes.map((x, j) =>
        cambio.principal ? { ...x, principal: j === i } : j === i ? { ...x, ...cambio } : x,
      ),
    });

  if (id && detalle.cargando && !detalle.datos) return <Cargando />;

  return (
    <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
      <h2>{id ? `Editar ${id}` : 'Nuevo apartamento'}</h2>
      <div className="fila">
        <label className="campo">
          <span>Identificación</span>
          <input required disabled={!!id} value={d.identificacion} placeholder="SUQ-401"
            onChange={(e) => setD({ ...d, identificacion: e.target.value })} />
        </label>
        <label className="campo">
          <span>Nombre</span>
          <input required value={d.nombre} onChange={(e) => setD({ ...d, nombre: e.target.value })} />
        </label>
        <label className="campo">
          <span>Dormitorios</span>
          <input required type="number" min={1} value={d.dormitorios} onChange={(e) => setD({ ...d, dormitorios: Number(e.target.value) })} />
        </label>
        <label className="campo">
          <span>Capacidad</span>
          <input required type="number" min={1} value={d.capacidad} onChange={(e) => setD({ ...d, capacidad: Number(e.target.value) })} />
        </label>
        <label className="campo campo--casilla">
          <input type="checkbox" checked={d.admiteMascotas} onChange={(e) => setD({ ...d, admiteMascotas: e.target.checked })} />
          <span>Admite mascotas</span>
        </label>
      </div>
      <label className="campo">
        <span>Descripción</span>
        <textarea required value={d.descripcion} onChange={(e) => setD({ ...d, descripcion: e.target.value })} />
      </label>
      <label className="campo">
        <span>Características (separadas por coma)</span>
        <input value={caracteristicas} onChange={(e) => setCaracteristicas(e.target.value)} placeholder="Wifi, Balcón, Cocina equipada" />
      </label>
      <label className="campo">
        <span>Advertencia de acceso</span>
        <input value={d.advertenciaAcceso ?? ''} onChange={(e) => setD({ ...d, advertenciaAcceso: e.target.value })} placeholder="Tercer piso sin ascensor" />
      </label>
      <fieldset className="grupo-campos">
        <legend>Imágenes</legend>
        <ul className="lista-editable">
          {d.imagenes.map((im, i) => (
            <li key={i}>
              <input type="url" required value={im.url} placeholder="https://" aria-label={`URL de la imagen ${i + 1}`}
                onChange={(e) => imagen(i, { url: e.target.value })} />
              <label className="campo--casilla">
                <input type="radio" name="principal" checked={im.principal} onChange={() => imagen(i, { principal: true })} />
                <span>Principal</span>
              </label>
              {d.imagenes.length > 1 && (
                <button type="button" className="boton boton--texto"
                  onClick={() => {
                    const resto = d.imagenes.filter((_, j) => j !== i);
                    if (im.principal) resto[0] = { ...resto[0], principal: true };
                    setD({ ...d, imagenes: resto });
                  }}>Quitar</button>
              )}
            </li>
          ))}
        </ul>
        {d.imagenes.length < 10 && (
          <div>
            <button type="button" className="boton boton--claro" onClick={() => setD({ ...d, imagenes: [...d.imagenes, { url: '', principal: false }] })}>
              Agregar imagen
            </button>
          </div>
        )}
      </fieldset>
      <AvisoError error={accion.error ?? detalle.error} />
      <div>
        <button className="boton boton--principal" disabled={accion.enCurso}>{id ? 'Guardar cambios' : 'Crear apartamento'}</button>
      </div>
    </form>
  );
}

/** Tarifa por ocupante facturable y noche en cada temporada (7.4), con el histórico. */
function Tarifas({ id, alCambiar }: { id: string; alCambiar: () => void }) {
  const temporadas = useCarga(() => administracion.temporadas(), []);
  const tarifario = useCarga(() => administracion.tarifas(id), [id]);
  const [valores, setValores] = useState<Record<string, string>>({});
  const accion = useAccion();
  const [ok, setOk] = useState(false);
  const [verHistorico, setVerHistorico] = useState(false);

  useEffect(() => {
    if (!tarifario.datos) return;
    setValores(Object.fromEntries(tarifario.datos.vigentes.map((t) => [t.idTemporada, String(t.valor)])));
  }, [tarifario.datos]);

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    setOk(false);
    const tarifas = Object.entries(valores)
      .filter(([, v]) => v !== '')
      .map(([idTemporada, v]) => ({ idTemporada, valor: Number(v) }));
    const r = await accion.ejecutar(() => administracion.fijarTarifas(id, tarifas));
    if (r) {
      tarifario.setDatos(r);
      setOk(true);
      alCambiar();
    }
  };

  return (
    <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--maiz)' } as React.CSSProperties}>
      <div className="titulo-con-accion">
        <h2>Tarifas de {id}</h2>
        {tarifario.datos && (
          <span className="chip" style={{ '--chip': tarifario.datos.completo ? 'var(--cafetal)' : 'var(--balcon)', '--chip-texto': 'var(--cal)' } as React.CSSProperties}>
            {tarifario.datos.completo ? 'Completas' : 'Incompletas'}
          </span>
        )}
      </div>
      {(temporadas.cargando || tarifario.cargando) && <Cargando />}
      <div className="fila">
        {temporadas.datos?.map((t) => (
          <label className="campo" key={t.id}>
            <span>{t.nombre}</span>
            <input type="number" min={0} step={1000} value={valores[t.id] ?? ''} placeholder="Sin tarifa"
              onChange={(e) => setValores({ ...valores, [t.id]: e.target.value })} />
          </label>
        ))}
      </div>
      {ok && <AvisoOk>Tarifas guardadas.</AvisoOk>}
      <AvisoError error={accion.error ?? temporadas.error ?? tarifario.error} />
      <div className="acciones acciones--fila">
        <button className="boton" disabled={accion.enCurso}>Guardar tarifas</button>
        {tarifario.datos && tarifario.datos.historico.length > 0 && (
          <button type="button" className="boton boton--texto" onClick={() => setVerHistorico((v) => !v)} aria-expanded={verHistorico}>
            {verHistorico ? 'Ocultar histórico' : 'Ver histórico'}
          </button>
        )}
      </div>
      {verHistorico && tarifario.datos && (
        <div className="tabla-envoltura">
          <table className="tabla tabla--compacta">
            <thead>
              <tr><th>Temporada</th><th className="num">Valor</th><th>Vigente desde</th></tr>
            </thead>
            <tbody>
              {tarifario.datos.historico.map((t, i) => (
                <tr key={i}><td>{t.temporada}</td><td className="num">{dinero(t.valor)}</td><td>{fechaHora(t.vigenteDesde)}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </form>
  );
}

/** Bloqueo por rango y motivo (7.3): rechazado si hay reservas activas en esas noches. */
function Bloqueo({ id }: { id: string }) {
  const [inicio, setInicio] = useState(hoy());
  const [fin, setFin] = useState(sumarDias(hoy(), 1));
  const [motivo, setMotivo] = useState('');
  const accion = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  const bloquear = async (e: FormEvent) => {
    e.preventDefault();
    setOk(null);
    const b = await accion.ejecutar(() => administracion.bloquear(id, inicio, fin, motivo));
    if (b) {
      setOk(`Bloqueado del ${b.fechaInicio} al ${b.fechaFin}.`);
      setMotivo('');
    }
  };

  return (
    <form className="placa pila" onSubmit={bloquear} style={{ '--losa': 'var(--balcon)' } as React.CSSProperties}>
      <h2>Bloquear {id}</h2>
      <div className="fila">
        <label className="campo">
          <span>Desde</span>
          <input type="date" required min={hoy()} value={inicio}
            onChange={(e) => { setInicio(e.target.value); if (fin <= e.target.value) setFin(sumarDias(e.target.value, 1)); }} />
        </label>
        <label className="campo">
          <span>Hasta (sin incluir)</span>
          <input type="date" required min={sumarDias(inicio, 1)} value={fin} onChange={(e) => setFin(e.target.value)} />
        </label>
        <label className="campo">
          <span>Motivo</span>
          <input required value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Pintura, reparación…" />
        </label>
      </div>
      {ok && <AvisoOk>{ok}</AvisoOk>}
      <AvisoError error={accion.error} />
      <div>
        <button className="boton boton--principal" disabled={accion.enCurso}>Registrar bloqueo</button>
      </div>
    </form>
  );
}

/** Inventario completo (7.3): crear, editar, activar, retirar, tarifas y bloqueos. */
export function AdminApartamentos() {
  const [pagina, setPagina] = useState(0);
  const inventario = useCarga(() => administracion.inventario(pagina), [pagina]);
  const [elegido, setElegido] = useState<string | null>(null);
  const [creando, setCreando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [retirando, setRetirando] = useState<string | null>(null);
  const accion = useAccion();

  const operar = async (op: () => Promise<unknown>, texto: string) => {
    setAviso(null);
    const ok = await accion.ejecutar(async () => {
      await op();
      return true;
    });
    if (ok) {
      setAviso(texto);
      inventario.recargar();
    }
  };

  return (
    <div className="pila-grande">
      <section>
        <div className="titulo-con-accion">
          <h2>Inventario</h2>
          <button className="boton boton--principal" onClick={() => { setCreando(true); setElegido(null); setAviso(null); }}>
            Nuevo apartamento
          </button>
        </div>
        {aviso && <AvisoOk>{aviso}</AvisoOk>}
        <AvisoError error={accion.error ?? inventario.error} />
        {inventario.cargando && !inventario.datos && <Cargando />}
        {inventario.datos && (
          <div className="placa placa--plana tabla-envoltura">
            <table className="tabla tabla--filas">
              <thead>
                <tr>
                  <th>Apartamento</th><th>Capacidad</th><th>Estado</th><th>Tarifas</th><th>Venta</th><th />
                </tr>
              </thead>
              <tbody>
                {inventario.datos.contenido.map((a) => (
                  <tr key={a.identificacion} aria-selected={elegido === a.identificacion}>
                    <td>
                      <span className="movimiento__apto movimiento__apto--chico"
                        style={{ background: variable(colorDe(a.identificacion)), color: textoSobre(colorDe(a.identificacion)) }}>
                        {a.identificacion.replace('SUQ-', '')}
                      </span>{' '}
                      <strong>{a.nombre}</strong>
                    </td>
                    <td>{a.dormitorios} dorm., {a.capacidad} pers.</td>
                    <td><EstadoOperativoChip estado={a.estadoOperativo} /></td>
                    <td>{a.tarifasCompletas ? 'Completas' : 'Incompletas'}</td>
                    <td>{a.activo ? 'Activo' : 'Inactivo'}</td>
                    <td className="acciones-tabla">
                      <button className="boton boton--texto" onClick={() => { setElegido(a.identificacion); setCreando(false); setAviso(null); }}>
                        Gestionar
                      </button>
                      {!a.activo && (
                        <button className="boton boton--texto" disabled={accion.enCurso || !a.tarifasCompletas}
                          title={a.tarifasCompletas ? undefined : 'Necesita tarifa en todas las temporadas'}
                          onClick={() => operar(() => administracion.activar(a.identificacion), `${a.identificacion} activo y a la venta.`)}>
                          Activar
                        </button>
                      )}
                      {a.activo && retirando !== a.identificacion && (
                        <button className="boton boton--texto" disabled={accion.enCurso} onClick={() => setRetirando(a.identificacion)}>
                          Retirar
                        </button>
                      )}
                      {a.activo && retirando === a.identificacion && (
                        <>
                          <button className="boton boton--texto boton--peligro" disabled={accion.enCurso}
                            onClick={() => { setRetirando(null); operar(() => administracion.retirar(a.identificacion), `${a.identificacion} retirado de la venta.`); }}>
                            Confirmar retiro
                          </button>
                          <button className="boton boton--texto" onClick={() => setRetirando(null)}>No</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {inventario.datos && inventario.datos.totalPaginas > 1 && (
          <Paginacion pagina={inventario.datos.pagina} totalPaginas={inventario.datos.totalPaginas}
            totalElementos={inventario.datos.totalElementos} alCambiar={setPagina} />
        )}
      </section>

      {creando && (
        <EditorApartamento id={null} alGuardar={(id, texto) => { setAviso(texto); setCreando(false); setElegido(id); inventario.recargar(); }} />
      )}
      {elegido && (
        <>
          <EditorApartamento id={elegido} alGuardar={(_, texto) => { setAviso(texto); inventario.recargar(); }} />
          <Tarifas id={elegido} alCambiar={() => inventario.recargar()} />
          <Bloqueo id={elegido} />
        </>
      )}
    </div>
  );
}

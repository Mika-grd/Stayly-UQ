import { useState, type FormEvent } from 'react';
import { folios } from '../api/servicios';
import type { Folio, MedioPago } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { dinero, fechaHora, nombreCargo, nombreMedio } from '../lib/formato';
import { useAccion, useCarga } from '../lib/useCarga';
import { AvisoError, AvisoOk, Cargando } from './Comunes';

function ahoraLocal() {
  const d = new Date();
  d.setSeconds(0, 0);
  return new Date(d.getTime() - d.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}

/** Folio de la reserva (7.7): cargos, pagos y saldo. Recepción registra movimientos y lo cierra. */
export function FolioPanel({ codigo, alCambiar }: { codigo: string; alCambiar: () => void }) {
  const { es } = useSesion();
  const personal = es('RECEPCIONISTA', 'ADMINISTRADOR');
  const esAdmin = es('ADMINISTRADOR');
  const carga = useCarga(() => folios.consultar(codigo), [codigo]);
  const accion = useAccion();
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [formulario, setFormulario] = useState<'pago' | 'cargo' | 'cierre' | null>(null);

  const aplicar = async (operacion: () => Promise<Folio>, texto: string) => {
    setMensaje(null);
    const folio = await accion.ejecutar(operacion);
    if (folio) {
      carga.setDatos(folio);
      setMensaje(texto);
      setFormulario(null);
      alCambiar();
    }
  };

  if (carga.cargando && !carga.datos) return <Cargando texto="Cargando el folio…" />;
  if (!carga.datos) return <AvisoError error={carga.error} />;
  const f = carga.datos;
  const abierto = f.estado === 'ABIERTO';

  return (
    <section className="folio placa" style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
      <div className="folio__cabeza">
        <h2>Folio</h2>
        <span className="chip" style={{ '--chip': abierto ? 'var(--maiz)' : 'var(--muro)' } as React.CSSProperties}>
          {abierto ? 'Abierto' : 'Cerrado'}
        </span>
      </div>

      <div className="folio__saldos">
        <div>
          <span>Cargos</span>
          <strong className="cifra">{dinero(f.totalCargos)}</strong>
        </div>
        <div>
          <span>Pagos</span>
          <strong className="cifra">{dinero(f.totalPagos)}</strong>
        </div>
        <div className={f.saldo > 0 ? 'folio__saldo folio__saldo--debe' : 'folio__saldo'}>
          <span>{f.saldo > 0 ? 'Saldo pendiente' : f.saldo < 0 ? 'Saldo a favor' : 'Saldo'}</span>
          <strong className="cifra">{dinero(Math.abs(f.saldo))}</strong>
        </div>
      </div>

      <div className="tabla-envoltura">
        <table className="tabla">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Movimiento</th>
              <th>Detalle</th>
              <th className="num">Valor</th>
            </tr>
          </thead>
          <tbody>
            {f.cargos.map((c, i) => (
              <tr key={`c${i}`}>
                <td>{fechaHora(c.fecha)}</td>
                <td>{nombreCargo[c.tipo]}</td>
                <td>{c.concepto}</td>
                <td className="num">{dinero(c.valor)}</td>
              </tr>
            ))}
            {f.pagos.map((p, i) => (
              <tr key={`p${i}`} className="folio__pago">
                <td>{fechaHora(p.fecha)}</td>
                <td>Pago con {nombreMedio[p.medio].toLowerCase()}</td>
                <td>{p.referencia ?? ''}</td>
                <td className="num">{dinero(-p.valor)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {f.autorizacionCierre && (
        <p className="nota">
          Cerrado con saldo por {f.autorizacionCierre.autor}: {f.autorizacionCierre.motivo}
        </p>
      )}

      {mensaje && <AvisoOk>{mensaje}</AvisoOk>}
      <AvisoError error={accion.error} />

      {personal && abierto && (
        <div className="folio__acciones">
          <button className="boton boton--cafetal" onClick={() => setFormulario(formulario === 'pago' ? null : 'pago')}>
            Registrar pago
          </button>
          <button className="boton boton--claro" onClick={() => setFormulario(formulario === 'cargo' ? null : 'cargo')}>
            Registrar cargo
          </button>
          <button className="boton boton--claro" onClick={() => setFormulario(formulario === 'cierre' ? null : 'cierre')}>
            Cerrar folio
          </button>
        </div>
      )}

      {formulario === 'pago' && (
        <FormularioPago
          saldo={f.saldo}
          enCurso={accion.enCurso}
          alEnviar={(p) => aplicar(() => folios.pagar(codigo, p), 'Pago registrado.')}
        />
      )}
      {formulario === 'cargo' && (
        <FormularioCargo
          enCurso={accion.enCurso}
          alEnviar={(c) => aplicar(() => folios.cargar(codigo, c), 'Cargo registrado.')}
        />
      )}
      {formulario === 'cierre' && (
        <FormularioCierre
          saldo={f.saldo}
          esAdmin={esAdmin}
          enCurso={accion.enCurso}
          alEnviar={(motivo) => aplicar(() => folios.cerrar(codigo, motivo), 'Folio cerrado.')}
        />
      )}
    </section>
  );
}

function FormularioPago({
  saldo,
  enCurso,
  alEnviar,
}: {
  saldo: number;
  enCurso: boolean;
  alEnviar: (p: { valor: number; medio: MedioPago; fecha: string; referencia?: string }) => void;
}) {
  const [valor, setValor] = useState(saldo > 0 ? saldo : 0);
  const [medio, setMedio] = useState<MedioPago>('EFECTIVO');
  const [fecha, setFecha] = useState(ahoraLocal());
  const [referencia, setReferencia] = useState('');
  const enviar = (e: FormEvent) => {
    e.preventDefault();
    alEnviar({ valor, medio, fecha: `${fecha}:00`, referencia: referencia || undefined });
  };
  return (
    <form className="subformulario" onSubmit={enviar}>
      <div className="fila">
        <label className="campo">
          <span>Valor</span>
          <input type="number" required step={1} value={valor} onChange={(e) => setValor(Number(e.target.value))} />
          <small>Negativo solo para corregir un pago anterior.</small>
        </label>
        <label className="campo">
          <span>Medio</span>
          <select value={medio} onChange={(e) => setMedio(e.target.value as MedioPago)}>
            {(Object.keys(nombreMedio) as MedioPago[]).map((m) => (
              <option key={m} value={m}>
                {nombreMedio[m]}
              </option>
            ))}
          </select>
        </label>
        <label className="campo">
          <span>Fecha y hora</span>
          <input type="datetime-local" required value={fecha} onChange={(e) => setFecha(e.target.value)} />
        </label>
        <label className="campo">
          <span>Referencia</span>
          <input value={referencia} onChange={(e) => setReferencia(e.target.value)} placeholder="Opcional" />
        </label>
      </div>
      <button className="boton boton--cafetal" disabled={enCurso}>
        Registrar pago
      </button>
    </form>
  );
}

function FormularioCargo({
  enCurso,
  alEnviar,
}: {
  enCurso: boolean;
  alEnviar: (c: { tipo: 'SERVICIO_ADICIONAL' | 'AJUSTE'; concepto: string; valor: number }) => void;
}) {
  const [tipo, setTipo] = useState<'SERVICIO_ADICIONAL' | 'AJUSTE'>('SERVICIO_ADICIONAL');
  const [concepto, setConcepto] = useState('');
  const [valor, setValor] = useState(0);
  return (
    <form
      className="subformulario"
      onSubmit={(e) => {
        e.preventDefault();
        alEnviar({ tipo, concepto, valor });
      }}
    >
      <div className="fila">
        <label className="campo">
          <span>Tipo</span>
          <select value={tipo} onChange={(e) => setTipo(e.target.value as typeof tipo)}>
            <option value="SERVICIO_ADICIONAL">Servicio adicional</option>
            <option value="AJUSTE">Ajuste</option>
          </select>
        </label>
        <label className="campo">
          <span>Concepto</span>
          <input required value={concepto} onChange={(e) => setConcepto(e.target.value)} placeholder="Parqueadero, desayuno…" />
        </label>
        <label className="campo">
          <span>Valor</span>
          <input type="number" required step={1} value={valor} onChange={(e) => setValor(Number(e.target.value))} />
        </label>
      </div>
      <button className="boton" disabled={enCurso}>
        Registrar cargo
      </button>
    </form>
  );
}

function FormularioCierre({
  saldo,
  esAdmin,
  enCurso,
  alEnviar,
}: {
  saldo: number;
  esAdmin: boolean;
  enCurso: boolean;
  alEnviar: (motivo?: string) => void;
}) {
  const [motivo, setMotivo] = useState('');
  if (saldo === 0) {
    return (
      <div className="subformulario">
        <p>El saldo es cero: el folio se puede cerrar. Después ya no admite movimientos.</p>
        <button className="boton" disabled={enCurso} onClick={() => alEnviar()}>
          Cerrar folio
        </button>
      </div>
    );
  }
  if (!esAdmin) {
    return (
      <div className="subformulario">
        <p>
          El folio tiene {saldo > 0 ? 'saldo pendiente' : 'saldo a favor'} de {dinero(Math.abs(saldo))}. Registre el pago
          o la devolución, o pida al administrador que lo cierre con su autorización.
        </p>
      </div>
    );
  }
  return (
    <form
      className="subformulario"
      onSubmit={(e) => {
        e.preventDefault();
        alEnviar(motivo);
      }}
    >
      <label className="campo">
        <span>Motivo de la autorización</span>
        <textarea required value={motivo} onChange={(e) => setMotivo(e.target.value)} />
        <small>Queda registrado con su nombre: el folio se cierra con saldo de {dinero(saldo)}.</small>
      </label>
      <button className="boton boton--principal" disabled={enCurso}>
        Autorizar y cerrar
      </button>
    </form>
  );
}

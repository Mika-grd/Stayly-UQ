import { useState } from 'react';
import { administracion, type ReporteCanales, type ReporteIngresos, type ReporteOcupacion } from '../../api/admin';
import type { MedioPago } from '../../api/tipos';
import { AvisoError, Cargando } from '../../components/Comunes';
import { dinero, fechaCorta, hoy, iso, nombreCanal, nombreMedio, sumarDias } from '../../lib/formato';
import { useCarga } from '../../lib/useCarga';

const COLOR_MEDIO: Record<MedioPago, string> = { EFECTIVO: 'var(--cafetal)', TRANSFERENCIA: 'var(--cobalto)', TARJETA: 'var(--lila)' };
const porcentaje = new Intl.NumberFormat('es-CO', { style: 'percent', maximumFractionDigits: 1 });

/**
 * Exportación de reportes (funcionalidad opcional 8, L-23): CSV con separador ";" y BOM, que Excel
 * en español abre directo como hoja de cálculo. Los números van sin formato para poder sumarlos.
 */
function exportarCsv(desde: string, hasta: string, ocupacion: ReporteOcupacion, ingresos: ReporteIngresos, canales: ReporteCanales) {
  const filas: (string | number)[][] = [
    ['Reportes Stayly-UQ', `${desde} a ${hasta} (sin incluir el último día)`],
    [],
    ['Ocupación'],
    ['Apartamentos activos', 'Noches del periodo', 'Noches vendidas', 'Ocupación (%)'],
    [ocupacion.apartamentosActivos, ocupacion.nochesPeriodo, ocupacion.nochesVendidas, (ocupacion.porcentajeOcupacion * 100).toFixed(2).replace('.', ',')],
    [],
    ['Ingresos (pagos registrados, COP)'],
    ['Medio de pago', 'Total'],
    ...ingresos.porMedioPago.map((m) => [nombreMedio[m.medio], m.total]),
    ['Total', ingresos.total],
    [],
    ['Reservas por canal'],
    ['Canal', 'Total', 'Canceladas', 'No-show'],
    ...canales.porCanal.map((c) => [nombreCanal[c.canal], c.total, c.canceladas, c.noShow]),
  ];
  const celda = (v: string | number) => {
    const t = String(v);
    return /[;"\n]/.test(t) ? `"${t.replace(/"/g, '""')}"` : t;
  };
  const csv = '﻿' + filas.map((f) => f.map(celda).join(';')).join('\r\n');
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = `reportes-stayly-${desde}-a-${hasta}.csv`;
  enlace.click();
  URL.revokeObjectURL(url);
}

/** Reportes del administrador (7.9) con filtro por rango de fechas [desde, hasta). */
export function AdminReportes() {
  const inicioMes = iso(new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  const [desde, setDesde] = useState(inicioMes);
  const [hasta, setHasta] = useState(sumarDias(hoy(), 1));
  const ocupacion = useCarga(() => administracion.ocupacion(desde, hasta), [desde, hasta]);
  const ingresos = useCarga(() => administracion.ingresos(desde, hasta), [desde, hasta]);
  const canales = useCarga(() => administracion.porCanal(desde, hasta), [desde, hasta]);
  const listos = ocupacion.datos && ingresos.datos && canales.datos;
  const maxCanal = Math.max(1, ...(canales.datos?.porCanal.map((c) => c.total) ?? [1]));

  return (
    <div className="pila-grande">
      <div className="barra">
        <label className="campo">
          <span>Desde</span>
          <input type="date" value={desde} max={sumarDias(hasta, -1)} onChange={(e) => setDesde(e.target.value)} />
        </label>
        <label className="campo">
          <span>Hasta (sin incluir)</span>
          <input type="date" value={hasta} min={sumarDias(desde, 1)} onChange={(e) => setHasta(e.target.value)} />
        </label>
        <p className="barra__titulo">{fechaCorta(desde)} al {fechaCorta(sumarDias(hasta, -1))}</p>
        <button className="boton boton--claro" disabled={!listos}
          onClick={() => listos && exportarCsv(desde, hasta, ocupacion.datos!, ingresos.datos!, canales.datos!)}>
          Descargar hoja de cálculo
        </button>
      </div>
      <AvisoError error={ocupacion.error ?? ingresos.error ?? canales.error} />
      {!listos && <Cargando />}
      {listos && (
        <div className="reportes">
          <section className="placa" style={{ '--losa': 'var(--maiz)' } as React.CSSProperties}>
            <h2>Ocupación</h2>
            <p className="reporte__cifra cifra">{porcentaje.format(ocupacion.datos!.porcentajeOcupacion)}</p>
            <div className="barra-progreso" role="img" aria-label={`Ocupación ${porcentaje.format(ocupacion.datos!.porcentajeOcupacion)}`}>
              <span style={{ width: `${Math.min(100, ocupacion.datos!.porcentajeOcupacion * 100)}%` }} />
            </div>
            <p className="nota">
              {ocupacion.datos!.nochesVendidas} noches vendidas de {ocupacion.datos!.apartamentosActivos * ocupacion.datos!.nochesPeriodo} posibles
              ({ocupacion.datos!.apartamentosActivos} apartamentos × {ocupacion.datos!.nochesPeriodo} noches).
            </p>
          </section>

          <section className="placa" style={{ '--losa': 'var(--cafetal)' } as React.CSSProperties}>
            <h2>Ingresos</h2>
            <p className="reporte__cifra cifra">{dinero(ingresos.datos!.total)}</p>
            <ul className="reporte__lista">
              {ingresos.datos!.porMedioPago.map((m) => (
                <li key={m.medio}>
                  <span className="reporte__marca" style={{ background: COLOR_MEDIO[m.medio] }} />
                  <span>{nombreMedio[m.medio]}</span>
                  <strong className="cifra">{dinero(m.total)}</strong>
                </li>
              ))}
            </ul>
            <p className="nota">Solo pagos registrados; los cargos sin pagar no cuentan.</p>
          </section>

          <section className="placa" style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
            <h2>Reservas por canal</h2>
            <ul className="reporte__barras">
              {canales.datos!.porCanal.map((c) => (
                <li key={c.canal}>
                  <span>{nombreCanal[c.canal]}</span>
                  <span className="reporte__barra">
                    <span style={{ width: `${(c.total / maxCanal) * 100}%` }} />
                  </span>
                  <strong className="cifra">{c.total}</strong>
                  <span className="nota">
                    {c.canceladas} canceladas, {c.noShow} no-show
                  </span>
                </li>
              ))}
            </ul>
          </section>
        </div>
      )}
    </div>
  );
}

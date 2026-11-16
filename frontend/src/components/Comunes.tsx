import type { ReactNode } from 'react';
import type { EstadoOperativo, EstadoReserva } from '../api/tipos';
import { ErrorApi } from '../api/cliente';
import { estiloEstadoOperativo, estiloEstadoReserva } from '../lib/colores';
import { nombreEstado, nombreOperativo } from '../lib/formato';

export function EstadoReservaChip({ estado }: { estado: EstadoReserva }) {
  return (
    <span className="chip" style={estiloEstadoReserva(estado)}>
      {nombreEstado[estado]}
    </span>
  );
}

export function EstadoOperativoChip({ estado }: { estado: EstadoOperativo }) {
  return (
    <span className="chip" style={estiloEstadoOperativo(estado)}>
      {nombreOperativo[estado]}
    </span>
  );
}

/** Muestra el error de la API con su mensaje de negocio; la regla va aparte para quien opera. */
export function AvisoError({ error }: { error: unknown }) {
  if (!error) return null;
  const mensaje = error instanceof Error ? error.message : 'No se pudo completar la operación.';
  const regla = error instanceof ErrorApi && error.regla ? `Regla ${error.regla}` : null;
  return (
    <div className="aviso aviso--error" role="alert">
      <strong>{mensaje}</strong>
      {regla && <small>{regla}</small>}
    </div>
  );
}

export function AvisoOk({ children }: { children: ReactNode }) {
  return (
    <div className="aviso aviso--ok" role="status">
      {children}
    </div>
  );
}

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <p className="cargando" role="status">
      {texto}
    </p>
  );
}

export function Paginacion({
  pagina,
  totalPaginas,
  totalElementos,
  alCambiar,
}: {
  pagina: number;
  totalPaginas: number;
  totalElementos: number;
  alCambiar: (pagina: number) => void;
}) {
  if (totalPaginas <= 1) return null;
  return (
    <nav className="paginacion" aria-label="Paginación">
      <button className="boton boton--claro" disabled={pagina === 0} onClick={() => alCambiar(pagina - 1)}>
        Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas} ({totalElementos} en total)
      </span>
      <button className="boton boton--claro" disabled={pagina + 1 >= totalPaginas} onClick={() => alCambiar(pagina + 1)}>
        Siguiente
      </button>
    </nav>
  );
}

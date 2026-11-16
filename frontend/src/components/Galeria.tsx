import { useState } from 'react';
import type { ApartamentoDetalle } from '../api/tipos';
import { colorDe, variable } from '../lib/colores';
import { Fachada } from './Fachada';

/**
 * Galería del detalle (7.1): la imagen principal primero y las demás como miniaturas. Las que no
 * cargan se omiten; si no carga ninguna, queda la fachada dibujada del apartamento.
 */
export function Galeria({ apto }: { apto: ApartamentoDetalle }) {
  const ordenadas = [...apto.imagenes].sort((a, b) => Number(b.principal) - Number(a.principal));
  const [fallidas, setFallidas] = useState<Set<string>>(new Set());
  const visibles = ordenadas.filter((i) => !fallidas.has(i.url));
  const [actual, setActual] = useState(0);
  const losa = variable(colorDe(apto.identificacion));
  const imagen = visibles[Math.min(actual, visibles.length - 1)];
  const fallo = (url: string) => setFallidas((f) => new Set(f).add(url));

  return (
    <div className="galeria placa" style={{ '--losa': losa } as React.CSSProperties}>
      <div className="galeria__principal">
        {imagen ? (
          <img src={imagen.url} alt={`${apto.nombre}, foto ${actual + 1}`} onError={() => fallo(imagen.url)} />
        ) : (
          <Fachada identificacion={apto.identificacion} dormitorios={apto.dormitorios} titulo={apto.nombre} />
        )}
      </div>
      <div className="galeria__lado" style={{ background: losa }}>
        {visibles.length > 1 ? (
          <div className="galeria__miniaturas">
            {visibles.map((i, n) => (
              <button
                key={i.url}
                type="button"
                className="galeria__miniatura"
                aria-pressed={n === actual}
                aria-label={`Ver foto ${n + 1}`}
                onClick={() => setActual(n)}
              >
                <img src={i.url} alt="" onError={() => fallo(i.url)} />
              </button>
            ))}
          </div>
        ) : (
          <span className="galeria__codigo">{apto.identificacion}</span>
        )}
      </div>
      {/* Precarga oculta: descarta temprano las URL que no responden */}
      <div hidden>
        {ordenadas.map((i) => (
          <img key={i.url} src={i.url} alt="" onError={() => fallo(i.url)} />
        ))}
      </div>
    </div>
  );
}

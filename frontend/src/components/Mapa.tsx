import type { Ubicacion } from '../api/tipos';

const TOKEN_MAPBOX = import.meta.env.VITE_MAPBOX_TOKEN?.trim();
const ZOOM = 16;
const TESELA = 256;

/** Coordenadas de tesela (Web Mercator) con fracción, para ubicar el pin dentro de la tesela. */
function tesela(lat: number, lon: number, z: number) {
  const n = 2 ** z;
  const x = ((lon + 180) / 360) * n;
  const rad = (lat * Math.PI) / 180;
  const y = ((1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2) * n;
  return { x, y };
}

/**
 * Ubicación del alojamiento (7.1, 7.5). Con VITE_MAPBOX_TOKEN usa el mapa estático de Mapbox, el
 * servicio que recomienda el enunciado; sin token arma el mapa con teselas raster de OpenStreetMap
 * (imágenes simples, sin WebGL ni credenciales). Las coordenadas vienen del API.
 */
export function Mapa({ ubicacion, titulo }: { ubicacion: Ubicacion; titulo: string }) {
  const { latitud: lat, longitud: lon } = ubicacion;
  const enlace = `https://www.openstreetmap.org/?mlat=${lat}&mlon=${lon}#map=${ZOOM}/${lat}/${lon}`;

  if (TOKEN_MAPBOX) {
    const estilo = document.documentElement.dataset.tema === 'harsh' ? 'dark-v11' : 'streets-v12';
    const src = `https://api.mapbox.com/styles/v1/mapbox/${estilo}/static/pin-l+d62839(${lon},${lat})/${lon},${lat},15,0/640x360@2x?access_token=${TOKEN_MAPBOX}`;
    return (
      <a className="mapa" href={enlace} target="_blank" rel="noreferrer">
        <img src={src} alt={`Mapa: ${titulo}`} loading="lazy" />
      </a>
    );
  }

  // Rejilla de 5 x 3 teselas centrada en el punto; el contenedor recorta lo que sobra.
  const { x, y } = tesela(lat, lon, ZOOM);
  const cx = Math.floor(x);
  const cy = Math.floor(y);
  const columnas = [-2, -1, 0, 1, 2];
  const filas = [-1, 0, 1];
  // Desplazamiento para que el punto exacto quede en el centro del contenedor
  const dx = (x - cx + 2) * TESELA;
  const dy = (y - cy + 1) * TESELA;

  return (
    <div className="mapa" role="img" aria-label={`Mapa: ${titulo}`}>
      <div className="mapa__teselas" style={{ left: `calc(50% - ${dx}px)`, top: `calc(50% - ${dy}px)` }}>
        {filas.map((f) =>
          columnas.map((c) => (
            <img
              key={`${c},${f}`}
              src={`https://tile.openstreetmap.org/${ZOOM}/${cx + c}/${cy + f}.png`}
              alt=""
              loading="lazy"
              draggable={false}
              style={{ left: (c + 2) * TESELA, top: (f + 1) * TESELA }}
            />
          )),
        )}
      </div>
      <svg className="mapa__pin" viewBox="0 0 24 32" aria-hidden="true">
        <path d="M12 0a12 12 0 0 0-12 12c0 9 12 20 12 20s12-11 12-20A12 12 0 0 0 12 0Z" />
        <circle cx="12" cy="12" r="4.5" />
      </svg>
      <a className="mapa__enlace" href={enlace} target="_blank" rel="noreferrer">
        Abrir mapa
      </a>
      <span className="mapa__creditos">© OpenStreetMap</span>
    </div>
  );
}

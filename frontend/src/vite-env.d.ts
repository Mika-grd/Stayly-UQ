/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL pública del API, incluida la ruta /api (Guía 22, 6.1); sin ella se usa /api del mismo origen. */
  readonly VITE_API_URL?: string;
  /** Token público de Mapbox para el mapa del alojamiento; sin él se usa OpenStreetMap. */
  readonly VITE_MAPBOX_TOKEN?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Token público de Mapbox para el mapa del alojamiento; sin él se usa OpenStreetMap. */
  readonly VITE_MAPBOX_TOKEN?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// Tipos y servicios de la administración (7.1 Administrador, 7.3, 7.4, 7.8, 7.9) según docs/api/openapi.yaml.
import { api, consulta } from './cliente';
import type { ApartamentoDetalle, CanalOrigen, EstadoOperativo, MedioPago, Pagina, Rol, Usuario } from './tipos';

export interface ApartamentoInventario {
  identificacion: string;
  nombre: string;
  dormitorios: number;
  capacidad: number;
  activo: boolean;
  estadoOperativo: EstadoOperativo;
  tarifasCompletas: boolean;
}

export interface ApartamentoDatos {
  identificacion: string;
  nombre: string;
  descripcion: string;
  dormitorios: number;
  capacidad: number;
  caracteristicas: string[];
  admiteMascotas: boolean;
  advertenciaAcceso?: string | null;
  imagenes: { url: string; principal: boolean }[];
}

export interface Periodo {
  fechaInicio: string;
  fechaFin: string;
}

export interface Temporada {
  id: string;
  nombre: string;
  periodos: Periodo[];
  esBase: boolean;
  estanciaMinima: number;
}

export interface TarifaVigente {
  idTemporada: string;
  temporada: string;
  valor: number;
  vigenteDesde: string;
}

export interface Tarifario {
  identificacionApartamento: string;
  vigentes: TarifaVigente[];
  historico: TarifaVigente[];
  completo: boolean;
}

export interface Bloqueo {
  id: string;
  identificacionApartamento: string;
  fechaInicio: string;
  fechaFin: string;
  motivo: string;
  autor: string;
  fechaRegistro: string;
}

export interface TramoPolitica {
  antelacionMinimaDias: number;
  porcentajeRetencion: number;
}

export interface PoliticaVersion {
  version: number;
  vigenteDesde: string;
  tramos: TramoPolitica[];
  porcentajeRetencionNoShow: number;
}

export interface ServicioAdicional {
  nombre: string;
  generaCargo: boolean;
  valor: number | null;
}

export interface ParametrosAlojamiento {
  umbralEdadFacturable: number;
  tiempoPreparacionHoras: number;
  plazoConfirmacionHoras: number;
  horaLimiteNoShow: string;
  exigeAnticipo: boolean;
  porcentajeAnticipo: number | null;
  mediosPagoAceptados: MedioPago[];
}

export interface ReglasPropias {
  cupoMascotas: number;
  cargoPorMascotaNoche: number;
  horaInicioLlegadaNocturna: string;
  valorRecargoLlegadaNocturna: number;
}

export interface Alojamiento {
  nombre: string;
  descripcion: string;
  ciudad: string;
  direccion: string;
  ubicacion: { latitud: number; longitud: number };
  horaEntrada: string;
  horaSalida: string;
  normasConvivencia: string[];
  serviciosAdicionales: ServicioAdicional[];
  parametros: ParametrosAlojamiento;
  reglasPropias?: ReglasPropias;
}

export interface UsuarioAdmin {
  nombre: string;
  correo: string;
  telefono: string;
  documentoIdentidad: string;
  fechaNacimiento: string;
  rol: Rol;
  contrasena?: string;
}

export interface Canal {
  id: string;
  nombre: string;
  tipo: CanalOrigen;
  activo: boolean;
  credencialGenerada: string | null;
}

export type TipoEventoCanal =
  | 'DISPONIBILIDAD_CONSULTADA'
  | 'RESERVA_RECIBIDA'
  | 'RESERVA_DUPLICADA'
  | 'CONFLICTO_REGISTRADO'
  | 'CANCELACION_RECIBIDA';

export interface EventoCanal {
  id: string;
  canal: string;
  tipo: TipoEventoCanal;
  identificadorExterno: string | null;
  fecha: string;
  resultado: string;
}

export interface ConflictoCanal {
  id: string;
  canal: string;
  identificadorExterno: string;
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  codigoReservaVigente: string;
  estado: 'PENDIENTE' | 'RESUELTO';
  fechaRegistro: string;
  resolucion: string | null;
}

export interface ReporteOcupacion {
  desde: string;
  hasta: string;
  apartamentosActivos: number;
  nochesPeriodo: number;
  nochesVendidas: number;
  porcentajeOcupacion: number;
}

export interface ReporteIngresos {
  desde: string;
  hasta: string;
  total: number;
  porMedioPago: { medio: MedioPago; total: number }[];
}

export interface ReporteCanales {
  desde: string;
  hasta: string;
  porCanal: { canal: CanalOrigen; total: number; canceladas: number; noShow: number }[];
}

const a = (id: string) => `/api/apartamentos/${encodeURIComponent(id)}`;

export const administracion = {
  inventario: (pagina = 0) => api.get<Pagina<ApartamentoInventario>>(`/api/apartamentos${consulta({ pagina })}`),
  crearApartamento: (d: ApartamentoDatos) => api.post<ApartamentoDetalle>('/api/apartamentos', d),
  actualizarApartamento: (id: string, d: ApartamentoDatos) =>
    api.put<{ apartamento: ApartamentoDetalle; reservasSobreNuevaCapacidad: string[] }>(a(id), d),
  activar: (id: string) => api.put<ApartamentoDetalle>(`${a(id)}/activar`),
  retirar: (id: string) => api.delete<void>(a(id)),
  tarifas: (id: string) => api.get<Tarifario>(`${a(id)}/tarifas`),
  fijarTarifas: (id: string, tarifas: { idTemporada: string; valor: number }[]) =>
    api.put<Tarifario>(`${a(id)}/tarifas`, { tarifas }),
  bloquear: (id: string, fechaInicio: string, fechaFin: string, motivo: string) =>
    api.post<Bloqueo>(`${a(id)}/bloqueos`, { fechaInicio, fechaFin, motivo }),

  temporadas: () => api.get<Temporada[]>('/api/temporadas'),
  crearTemporada: (t: Omit<Temporada, 'id'>) => api.post<Temporada>('/api/temporadas', t),
  actualizarTemporada: (id: string, t: Omit<Temporada, 'id'>) => api.put<Temporada>(`/api/temporadas/${id}`, t),

  nuevaPolitica: (tramos: TramoPolitica[], porcentajeRetencionNoShow: number) =>
    api.post<PoliticaVersion>('/api/politica-cancelacion/versiones', { tramos, porcentajeRetencionNoShow }),

  alojamiento: () => api.get<Alojamiento>('/api/alojamiento'),
  guardarAlojamiento: (d: Omit<Alojamiento, 'reglasPropias'>) => api.put<Alojamiento>('/api/alojamiento', d),
  guardarReglasPropias: (r: ReglasPropias) => api.put<Alojamiento>('/api/alojamiento/parametros', r),

  usuarios: (pagina = 0) => api.get<Pagina<Usuario>>(`/api/usuarios${consulta({ pagina })}`),
  crearUsuario: (u: UsuarioAdmin) => api.post<Usuario>('/api/usuarios', u),
  actualizarUsuario: (id: string, u: UsuarioAdmin) => api.put<Usuario>(`/api/usuarios/${id}`, u),
  eliminarUsuario: (id: string) => api.delete<void>(`/api/usuarios/${id}`),

  canales: () => api.get<Canal[]>('/api/canales'),
  registrarCanal: (nombre: string, tipo: CanalOrigen) => api.post<Canal>('/api/canales', { nombre, tipo }),
  bitacora: (f: { canal?: string; desde?: string; hasta?: string; pagina?: number }) =>
    api.get<Pagina<EventoCanal>>(`/api/canales/bitacora${consulta({ ...f, pagina: f.pagina ?? 0 })}`),
  conflictos: (estado: '' | 'PENDIENTE' | 'RESUELTO', pagina = 0) =>
    api.get<Pagina<ConflictoCanal>>(`/api/conflictos-canal${consulta({ estado, pagina })}`),
  resolverConflicto: (id: string, resolucion: string) =>
    api.put<ConflictoCanal>(`/api/conflictos-canal/${id}/resolver`, { resolucion }),

  ocupacion: (desde: string, hasta: string) => api.get<ReporteOcupacion>(`/api/reportes/ocupacion${consulta({ desde, hasta })}`),
  ingresos: (desde: string, hasta: string) => api.get<ReporteIngresos>(`/api/reportes/ingresos${consulta({ desde, hasta })}`),
  porCanal: (desde: string, hasta: string) =>
    api.get<ReporteCanales>(`/api/reportes/reservas-por-canal${consulta({ desde, hasta })}`),
};

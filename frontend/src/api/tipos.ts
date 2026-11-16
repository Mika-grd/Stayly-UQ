// Tipos del contrato docs/api/openapi.yaml que usa la interfaz. Fechas en ISO (yyyy-MM-dd), horas HH:mm.

export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'ADMINISTRADOR' | 'PERSONAL_SERVICIO';

export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA' | 'NO_SHOW';
export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';
export type EstadoOperativo = 'PREPARADO' | 'OCUPADO' | 'PENDIENTE_PREPARACION' | 'EN_PREPARACION' | 'FUERA_DE_SERVICIO';
export type MedioPago = 'EFECTIVO' | 'TRANSFERENCIA' | 'TARJETA';
export type TipoCargo = 'ALOJAMIENTO' | 'SERVICIO_ADICIONAL' | 'AJUSTE' | 'PENALIDAD';

export interface ErrorNegocio {
  codigo: string;
  mensaje: string;
  regla?: string | null;
  ruta?: string;
}

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface Token {
  token: string;
  tipo: string;
  expiraEn: number;
  rol: Rol;
}

export interface Usuario {
  id: string;
  nombre: string;
  correo: string;
  telefono: string;
  rol: Rol;
  activo: boolean;
}

export interface ApartamentoResumen {
  identificacion: string;
  nombre: string;
  capacidad: number;
  dormitorios: number;
  imagenPrincipal: string | null;
  caracteristicas: string[];
  admiteMascotas: boolean;
  advertenciaAcceso: string | null;
  valorEstimado: number | null;
}

export interface ApartamentoDetalle {
  identificacion: string;
  nombre: string;
  descripcion: string;
  dormitorios: number;
  capacidad: number;
  caracteristicas: string[];
  admiteMascotas: boolean;
  advertenciaAcceso: string | null;
  imagenes: { url: string; principal: boolean }[];
  estadoOperativo: EstadoOperativo;
  activo: boolean;
}

export interface CargoNoche {
  fecha: string;
  temporada: string;
  tarifa: number;
  ocupantesFacturables: number;
  subtotal: number;
}

export interface Cotizacion {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  numeroNoches: number;
  ocupantesTotales: number;
  ocupantesFacturables: number;
  noches: CargoNoche[];
  valorAlojamiento: number;
  cargoMascotas: number | null;
  total: number;
}

export interface Ocupante {
  id: string;
  nombre: string;
  fechaNacimiento: string;
  facturable: boolean;
  titular: boolean;
}

export interface Reserva {
  codigo: string;
  estado: EstadoReserva;
  canal: CanalOrigen;
  identificacionApartamento: string;
  advertenciaAcceso: string | null;
  fechaEntrada: string;
  fechaSalida: string;
  numeroNoches: number;
  horaEstimadaLlegada: string | null;
  ocupantes: Ocupante[];
  mascotasAutorizadas: number;
  valor: { noches: CargoNoche[]; total: number };
  versionPolitica: number;
  saldoFolio: number;
  fechaCreacion: string;
}

export interface ReservaResumen {
  codigo: string;
  estado: EstadoReserva;
  canal: CanalOrigen;
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  nombreTitular: string;
  total: number;
  fechaCreacion: string;
}

export interface Liquidacion {
  codigoReserva: string;
  estado: 'CANCELADA' | 'NO_SHOW';
  versionPolitica: number;
  retencion: number;
  devolucion: number;
  saldoFolio: number;
}

export interface Folio {
  id: string;
  codigoReserva: string;
  estado: 'ABIERTO' | 'CERRADO';
  cargos: { tipo: TipoCargo; concepto: string; valor: number; fecha: string }[];
  pagos: { valor: number; medio: MedioPago; fecha: string; referencia: string | null }[];
  totalCargos: number;
  totalPagos: number;
  saldo: number;
  autorizacionCierre: { autor: string; motivo: string; fecha: string } | null;
}

export interface Politica {
  version: number;
  vigenteDesde: string;
  tramos: { antelacionMinimaDias: number; porcentajeRetencion: number }[];
  porcentajeRetencionNoShow: number;
}

export interface MovimientosDelDia {
  fecha: string;
  llegadas: ReservaResumen[];
  salidas: ReservaResumen[];
}

export interface ApartamentoPorPreparar {
  identificacion: string;
  nombre: string;
  estadoOperativo: EstadoOperativo;
}

export type EstadoNoche = 'LIBRE' | 'RESERVADA' | 'BLOQUEADA';

export interface Calendario {
  desde: string;
  hasta: string;
  apartamentos: {
    identificacion: string;
    noches: { fecha: string; estado: EstadoNoche; codigoReserva: string | null }[];
  }[];
}

export interface DatosTitular {
  nombre: string;
  documentoIdentidad: string;
  fechaNacimiento: string;
  correo: string;
  telefono: string;
}

export interface DatosAcompanante {
  nombre: string;
  fechaNacimiento: string;
  documentoIdentidad?: string;
}

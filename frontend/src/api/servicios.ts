import { api, consulta } from './cliente';
import type {
  ApartamentoDetalle,
  CanalOrigen,
  ApartamentoPorPreparar,
  ApartamentoResumen,
  Calendario,
  Cotizacion,
  DatosAcompanante,
  DatosTitular,
  DisponibilidadApartamento,
  GravedadNovedad,
  EstadoReserva,
  Folio,
  Liquidacion,
  MedioPago,
  MovimientosDelDia,
  Novedad,
  Pagina,
  Politica,
  Reserva,
  ReservaResumen,
  Token,
  Usuario,
} from './tipos';

const r = (codigo: string) => `/api/reservas/${encodeURIComponent(codigo)}`;
const a = (id: string) => `/api/apartamentos/${encodeURIComponent(id)}`;

export const sesion = {
  iniciar: (correo: string, contrasena: string) => api.post<Token>('/api/auth/login', { correo, contrasena }),
  registrar: (datos: {
    nombre: string;
    correo: string;
    contrasena: string;
    telefono: string;
    documentoIdentidad: string;
    fechaNacimiento: string;
  }) => api.post<Usuario>('/api/auth/registro', datos),
  yo: () => api.get<Usuario>('/api/usuarios/me'),
  actualizarPerfil: (p: { nombre: string; telefono: string; fotoUrl?: string | null }) =>
    api.put<Usuario>('/api/usuarios/me', { ...p, fotoUrl: p.fotoUrl || undefined }),
  cambiarContrasena: (contrasenaActual: string, contrasenaNueva: string) =>
    api.put<void>('/api/usuarios/me/contrasena', { contrasenaActual, contrasenaNueva }),
  solicitarRecuperacion: (correo: string) => api.post<void>('/api/auth/recuperacion', { correo }),
  confirmarRecuperacion: (correo: string, codigo: string, contrasenaNueva: string) =>
    api.post<void>('/api/auth/recuperacion/confirmar', { correo, codigo, contrasenaNueva }),
};

export const apartamentos = {
  disponibles: (p: {
    entrada: string;
    salida: string;
    fechasNacimiento: string[];
    pagina?: number;
    capacidadMinima?: number | null;
    precioMaximo?: number | null;
    caracteristicas?: string[];
  }) =>
    api.get<Pagina<ApartamentoResumen>>(
      `/api/apartamentos/disponibles${consulta({
        fechaEntrada: p.entrada,
        fechaSalida: p.salida,
        fechasNacimiento: p.fechasNacimiento,
        capacidadMinima: p.capacidadMinima ?? undefined,
        precioMaximo: p.precioMaximo ?? undefined,
        caracteristicas: p.caracteristicas ?? [],
        pagina: p.pagina ?? 0,
      })}`,
    ),
  disponibilidad: (id: string, desde: string, hasta: string) =>
    api.get<DisponibilidadApartamento>(`${a(id)}/disponibilidad${consulta({ desde, hasta })}`),
  novedades: (id: string, pagina = 0) => api.get<Pagina<Novedad>>(`${a(id)}/novedades${consulta({ pagina })}`),
  registrarNovedad: (id: string, descripcion: string, gravedad: GravedadNovedad) =>
    api.post<Novedad>(`${a(id)}/novedades`, { descripcion, gravedad }),
  detalle: (id: string) => api.get<ApartamentoDetalle>(a(id)),
  porPreparar: () => api.get<ApartamentoPorPreparar[]>('/api/apartamentos/por-preparar'),
  iniciarPreparacion: (id: string) => api.put(`${a(id)}/iniciar-preparacion`),
  marcarPreparado: (id: string) => api.put(`${a(id)}/marcar-preparado`),
  fueraDeServicio: (id: string, motivo: string) => api.put(`${a(id)}/declarar-fuera-de-servicio`, { motivo }),
  volverAServicio: (id: string) => api.put(`${a(id)}/volver-a-servicio`),
};

export const reservas = {
  cotizar: (p: { apartamento: string; entrada: string; salida: string; fechasNacimiento: string[]; mascotas: number }) =>
    api.post<Cotizacion>('/api/cotizaciones', {
      identificacionApartamento: p.apartamento,
      fechaEntrada: p.entrada,
      fechaSalida: p.salida,
      fechasNacimientoOcupantes: p.fechasNacimiento,
      mascotasAutorizadas: p.mascotas,
    }),
  crear: (p: {
    apartamento: string;
    entrada: string;
    salida: string;
    horaEstimadaLlegada?: string;
    titular: DatosTitular;
    acompanantes: DatosAcompanante[];
    mascotas: number;
  }) =>
    api.post<Reserva>('/api/reservas', {
      identificacionApartamento: p.apartamento,
      fechaEntrada: p.entrada,
      fechaSalida: p.salida,
      horaEstimadaLlegada: p.horaEstimadaLlegada || undefined,
      titular: p.titular,
      acompanantes: p.acompanantes,
      mascotasAutorizadas: p.mascotas,
    }),
  detalle: (codigo: string) => api.get<Reserva>(r(codigo)),
  mias: (pagina = 0) => api.get<Pagina<ReservaResumen>>(`/api/usuarios/me/reservas${consulta({ pagina })}`),
  listar: (f: {
    estado?: EstadoReserva | '';
    titular?: string;
    canal?: CanalOrigen | '';
    apartamento?: string;
    desde?: string;
    hasta?: string;
    pagina?: number;
  }) =>
    api.get<Pagina<ReservaResumen>>(
      `/api/reservas${consulta({
        estado: f.estado,
        titular: f.titular,
        canal: f.canal,
        apartamento: f.apartamento,
        desde: f.desde,
        hasta: f.hasta,
        pagina: f.pagina ?? 0,
      })}`,
    ),
  modificar: (
    codigo: string,
    p: { apartamento: string; entrada: string; salida: string; titular: DatosTitular; acompanantes: DatosAcompanante[]; mascotas: number },
  ) =>
    api.put<Reserva>(r(codigo), {
      identificacionApartamento: p.apartamento,
      fechaEntrada: p.entrada,
      fechaSalida: p.salida,
      titular: p.titular,
      acompanantes: p.acompanantes,
      mascotasAutorizadas: p.mascotas,
    }),
  movimientos: (fecha?: string) => api.get<MovimientosDelDia>(`/api/reservas/movimientos-del-dia${consulta({ fecha })}`),
  horaLlegada: (codigo: string, hora: string) => api.put<Reserva>(`${r(codigo)}/hora-llegada`, { horaEstimadaLlegada: hora }),
  confirmar: (codigo: string) => api.put<Reserva>(`${r(codigo)}/confirmar`),
  registrarLlegada: (codigo: string) => api.put<Reserva>(`${r(codigo)}/registrar-llegada`),
  registrarSalida: (codigo: string) => api.put<Reserva>(`${r(codigo)}/registrar-salida`),
  cancelar: (codigo: string) => api.put<Liquidacion>(`${r(codigo)}/cancelar`, {}),
  noShow: (codigo: string) => api.put<Liquidacion>(`${r(codigo)}/no-show`),
  politicaVigente: () => api.get<Politica>('/api/politica-cancelacion/vigente'),
};

export const folios = {
  consultar: (codigo: string) => api.get<Folio>(`${r(codigo)}/folio`),
  pagar: (codigo: string, p: { valor: number; medio: MedioPago; fecha: string; referencia?: string }) =>
    api.post<Folio>(`${r(codigo)}/folio/pagos`, p),
  cargar: (codigo: string, p: { tipo: 'SERVICIO_ADICIONAL' | 'AJUSTE'; concepto: string; valor: number }) =>
    api.post<Folio>(`${r(codigo)}/folio/cargos`, p),
  cerrar: (codigo: string, motivoAutorizacion?: string) =>
    api.put<Folio>(`${r(codigo)}/folio/cerrar`, motivoAutorizacion ? { motivoAutorizacion } : {}),
};

export const operacion = {
  resumenDelDia: (fecha: string) =>
    api.get<{ fecha: string; disponible: boolean; texto: string | null; proveedor: string }>(
      `/api/asistente/resumen-del-dia${consulta({ fecha })}`,
    ),
  calendario: (desde: string, hasta: string) =>
    api.get<Calendario>(`/api/calendario-ocupacion${consulta({ desde, hasta })}`),
};

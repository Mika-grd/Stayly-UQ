import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { alExpirarSesion, guardarSesion, leerSesion } from '../api/cliente';
import { sesion as servicioSesion } from '../api/servicios';
import type { Rol, Usuario } from '../api/tipos';

interface ContextoSesion {
  usuario: Usuario | null;
  rol: Rol | null;
  iniciar: (correo: string, contrasena: string) => Promise<Rol>;
  cerrar: () => void;
  actualizarUsuario: (u: Usuario) => void;
  es: (...roles: Rol[]) => boolean;
}

const Contexto = createContext<ContextoSesion | null>(null);

export function ProveedorSesion({ children }: { children: ReactNode }) {
  const [rol, setRol] = useState<Rol | null>(() => (leerSesion()?.rol as Rol) ?? null);
  const [usuario, setUsuario] = useState<Usuario | null>(null);

  const cerrar = useCallback(() => {
    guardarSesion(null);
    setRol(null);
    setUsuario(null);
  }, []);

  useEffect(() => {
    alExpirarSesion(cerrar);
  }, [cerrar]);

  useEffect(() => {
    if (rol && !usuario) {
      servicioSesion.yo().then(setUsuario).catch(() => undefined);
    }
  }, [rol, usuario]);

  const iniciar = useCallback(async (correo: string, contrasena: string) => {
    const token = await servicioSesion.iniciar(correo, contrasena);
    guardarSesion({ token: token.token, rol: token.rol, expira: Date.now() + token.expiraEn * 1000 });
    setUsuario(null);
    setRol(token.rol);
    return token.rol;
  }, []);

  const valor = useMemo<ContextoSesion>(
    () => ({ usuario, rol, iniciar, cerrar, actualizarUsuario: setUsuario, es: (...roles) => rol !== null && roles.includes(rol) }),
    [usuario, rol, iniciar, cerrar],
  );

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useSesion() {
  const contexto = useContext(Contexto);
  if (!contexto) throw new Error('useSesion fuera de ProveedorSesion');
  return contexto;
}

export const PERSONAL: Rol[] = ['RECEPCIONISTA', 'ADMINISTRADOR'];

/** Pide iniciar sesión (y volver aquí) o muestra el aviso si el rol no alcanza. */
export function Requiere({ roles, children }: { roles?: Rol[]; children: ReactNode }) {
  const { rol } = useSesion();
  const ubicacion = useLocation();
  if (!rol) {
    return <Navigate to={`/entrar?volver=${encodeURIComponent(ubicacion.pathname + ubicacion.search)}`} replace />;
  }
  if (roles && !roles.includes(rol)) {
    return (
      <main className="contenedor pagina">
        <div className="aviso aviso--error" role="alert">
          <strong>Esta sección no está disponible para su rol.</strong>
          Ingrese con una cuenta que tenga permiso o vuelva al inicio.
        </div>
      </main>
    );
  }
  return <>{children}</>;
}

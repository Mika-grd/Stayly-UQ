import { useState, type FormEvent } from 'react';
import { administracion, type UsuarioAdmin } from '../../api/admin';
import type { Rol, Usuario } from '../../api/tipos';
import { useSesion } from '../../auth/Sesion';
import { AvisoError, AvisoOk, Cargando, Paginacion } from '../../components/Comunes';
import { hoy, nombreRol } from '../../lib/formato';
import { useAccion, useCarga } from '../../lib/useCarga';

const VACIO: UsuarioAdmin = { nombre: '', correo: '', telefono: '', documentoIdentidad: '', fechaNacimiento: '', rol: 'RECEPCIONISTA', contrasena: '' };

/** Usuarios y roles (7.1, 7.2): el administrador asigna roles internos; la eliminación es lógica. */
export function AdminUsuarios() {
  const { usuario: yo } = useSesion();
  const [pagina, setPagina] = useState(0);
  const lista = useCarga(() => administracion.usuarios(pagina), [pagina]);
  const [editando, setEditando] = useState<Usuario | 'nuevo' | null>(null);
  const [u, setU] = useState<UsuarioAdmin>(VACIO);
  const [eliminando, setEliminando] = useState<string | null>(null);
  const accion = useAccion();
  const [ok, setOk] = useState<string | null>(null);

  const abrir = (x: Usuario | null) => {
    setOk(null);
    setEditando(x ?? 'nuevo');
    // El contrato no devuelve documento ni fecha de nacimiento: se piden de nuevo al editar
    setU(x ? { ...VACIO, nombre: x.nombre, correo: x.correo, telefono: x.telefono, rol: x.rol } : VACIO);
  };

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    const cuerpo = { ...u, contrasena: u.contrasena || undefined };
    const r = await accion.ejecutar(() =>
      editando === 'nuevo' ? administracion.crearUsuario(cuerpo) : administracion.actualizarUsuario((editando as Usuario).id, cuerpo),
    );
    if (r) {
      setOk(`${r.nombre} guardado como ${nombreRol[r.rol].toLowerCase()}.`);
      setEditando(null);
      lista.recargar();
    }
  };

  const eliminar = async (x: Usuario) => {
    setOk(null);
    setEliminando(null);
    const r = await accion.ejecutar(async () => {
      await administracion.eliminarUsuario(x.id);
      return true;
    });
    if (r) {
      setOk(`${x.nombre} quedó inactivo. Sus reservas históricas se conservan.`);
      lista.recargar();
    }
  };

  const campo = (k: keyof UsuarioAdmin) => ({
    value: (u[k] as string) ?? '',
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => setU({ ...u, [k]: e.target.value }),
  });

  return (
    <div className="pila-grande">
      <section>
        <div className="titulo-con-accion">
          <h2>Usuarios</h2>
          <button className="boton boton--principal" onClick={() => abrir(null)}>Nuevo usuario</button>
        </div>
        {ok && <AvisoOk>{ok}</AvisoOk>}
        <AvisoError error={accion.error ?? lista.error} />
        {lista.cargando && !lista.datos && <Cargando />}
        {lista.datos && (
          <div className="placa placa--plana tabla-envoltura">
            <table className="tabla tabla--filas">
              <thead>
                <tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th /></tr>
              </thead>
              <tbody>
                {lista.datos.contenido.map((x) => (
                  <tr key={x.id}>
                    <td><strong>{x.nombre}</strong></td>
                    <td>{x.correo}</td>
                    <td>{nombreRol[x.rol]}</td>
                    <td>{x.activo ? 'Activo' : 'Inactivo'}</td>
                    <td className="acciones-tabla">
                      {x.activo && <button className="boton boton--texto" onClick={() => abrir(x)}>Editar</button>}
                      {x.activo && x.id !== yo?.id && eliminando !== x.id && (
                        <button className="boton boton--texto" onClick={() => setEliminando(x.id)}>Eliminar</button>
                      )}
                      {eliminando === x.id && (
                        <>
                          <button className="boton boton--texto boton--peligro" disabled={accion.enCurso} onClick={() => eliminar(x)}>Confirmar</button>
                          <button className="boton boton--texto" onClick={() => setEliminando(null)}>No</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {lista.datos && lista.datos.totalPaginas > 1 && (
          <Paginacion pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} totalElementos={lista.datos.totalElementos} alCambiar={setPagina} />
        )}
      </section>

      {editando && (
        <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
          <h2>{editando === 'nuevo' ? 'Nuevo usuario' : `Editar ${(editando as Usuario).nombre}`}</h2>
          <div className="fila">
            <label className="campo"><span>Nombre</span><input required {...campo('nombre')} /></label>
            <label className="campo"><span>Correo</span><input required type="email" {...campo('correo')} /></label>
            <label className="campo"><span>Teléfono</span><input required {...campo('telefono')} /></label>
            <label className="campo"><span>Documento</span><input required inputMode="numeric" {...campo('documentoIdentidad')} /></label>
            <label className="campo"><span>Fecha de nacimiento</span><input required type="date" max={hoy()} {...campo('fechaNacimiento')} /></label>
            <label className="campo">
              <span>Rol</span>
              <select value={u.rol} onChange={(e) => setU({ ...u, rol: e.target.value as Rol })}>
                {(Object.keys(nombreRol) as Rol[]).map((r) => <option key={r} value={r}>{nombreRol[r]}</option>)}
              </select>
            </label>
            <label className="campo">
              <span>{editando === 'nuevo' ? 'Contraseña' : 'Nueva contraseña (opcional)'}</span>
              <input type="password" required={editando === 'nuevo'} minLength={8} pattern="(?=.*[A-Z])(?=.*\d).{8,}" autoComplete="new-password" {...campo('contrasena')} />
              <small>8+ caracteres, mayúscula y número</small>
            </label>
          </div>
          <AvisoError error={accion.error} />
          <div className="acciones acciones--fila">
            <button className="boton boton--principal" disabled={accion.enCurso}>Guardar</button>
            <button type="button" className="boton boton--claro" onClick={() => setEditando(null)}>Cancelar</button>
          </div>
        </form>
      )}
    </div>
  );
}

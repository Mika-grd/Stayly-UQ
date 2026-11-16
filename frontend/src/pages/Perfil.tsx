import { useEffect, useState, type FormEvent } from 'react';
import { sesion as servicioSesion } from '../api/servicios';
import { useSesion } from '../auth/Sesion';
import { AvisoError, AvisoOk, Cargando } from '../components/Comunes';
import { nombreRol } from '../lib/formato';
import { useAccion } from '../lib/useCarga';

/** Perfil propio (7.2, CU-39): nombre, teléfono y foto; el correo no se edita. Cambio de contraseña. */
export function Perfil() {
  const { usuario, actualizarUsuario } = useSesion();
  const datos = useAccion();
  const clave = useAccion();
  const [nombre, setNombre] = useState('');
  const [telefono, setTelefono] = useState('');
  const [fotoUrl, setFotoUrl] = useState('');
  const [fotoFallo, setFotoFallo] = useState(false);
  const [actual, setActual] = useState('');
  const [nueva, setNueva] = useState('');
  const [repetida, setRepetida] = useState('');
  const [okDatos, setOkDatos] = useState(false);
  const [okClave, setOkClave] = useState(false);

  useEffect(() => {
    if (!usuario) return;
    setNombre(usuario.nombre);
    setTelefono(usuario.telefono);
    setFotoUrl(usuario.fotoUrl ?? '');
  }, [usuario]);

  useEffect(() => setFotoFallo(false), [fotoUrl]);

  if (!usuario) return <main className="contenedor pagina"><Cargando /></main>;

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    setOkDatos(false);
    const u = await datos.ejecutar(() => servicioSesion.actualizarPerfil({ nombre, telefono, fotoUrl }));
    if (u) {
      actualizarUsuario(u);
      setOkDatos(true);
    }
  };

  const cambiarClave = async (e: FormEvent) => {
    e.preventDefault();
    setOkClave(false);
    if (nueva !== repetida) return;
    const ok = await clave.ejecutar(async () => {
      await servicioSesion.cambiarContrasena(actual, nueva);
      return true;
    });
    if (ok) {
      setActual('');
      setNueva('');
      setRepetida('');
      setOkClave(true);
    }
  };

  const iniciales = usuario.nombre
    .split(' ')
    .slice(0, 2)
    .map((p) => p[0])
    .join('');

  return (
    <main className="contenedor pagina perfil">
      <div className="perfil__cabeza">
        <div className="perfil__foto">
          {fotoUrl && !fotoFallo ? <img src={fotoUrl} alt="" onError={() => setFotoFallo(true)} /> : <span>{iniciales}</span>}
        </div>
        <div>
          <h1>{usuario.nombre}</h1>
          <p className="nota">
            {usuario.correo}, {nombreRol[usuario.rol]}
          </p>
        </div>
      </div>

      <div className="perfil__columnas">
        <form className="placa pila" onSubmit={guardar} style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
          <h2>Datos</h2>
          <label className="campo">
            <span>Nombre</span>
            <input required value={nombre} onChange={(e) => setNombre(e.target.value)} autoComplete="name" />
          </label>
          <label className="campo">
            <span>Correo</span>
            <input value={usuario.correo} disabled />
          </label>
          <label className="campo">
            <span>Teléfono</span>
            <input required value={telefono} onChange={(e) => setTelefono(e.target.value)} autoComplete="tel" />
          </label>
          <label className="campo">
            <span>Foto (URL)</span>
            <input type="url" value={fotoUrl} onChange={(e) => setFotoUrl(e.target.value)} placeholder="https://" />
          </label>
          {okDatos && <AvisoOk>Datos guardados.</AvisoOk>}
          <AvisoError error={datos.error} />
          <div>
            <button className="boton boton--principal" disabled={datos.enCurso}>
              Guardar
            </button>
          </div>
        </form>

        <form className="placa pila" onSubmit={cambiarClave} style={{ '--losa': 'var(--lila)' } as React.CSSProperties}>
          <h2>Contraseña</h2>
          <label className="campo">
            <span>Actual</span>
            <input type="password" required value={actual} onChange={(e) => setActual(e.target.value)} autoComplete="current-password" />
          </label>
          <label className="campo">
            <span>Nueva</span>
            <input
              type="password"
              required
              minLength={8}
              pattern="(?=.*[A-Z])(?=.*\d).{8,}"
              value={nueva}
              onChange={(e) => setNueva(e.target.value)}
              autoComplete="new-password"
            />
            <small>8+ caracteres, mayúscula y número</small>
          </label>
          <label className="campo">
            <span>Repetir la nueva</span>
            <input type="password" required value={repetida} onChange={(e) => setRepetida(e.target.value)} autoComplete="new-password" />
          </label>
          {repetida && nueva !== repetida && <p className="nota">No coinciden.</p>}
          {okClave && <AvisoOk>Contraseña cambiada.</AvisoOk>}
          <AvisoError error={clave.error} />
          <div>
            <button className="boton" disabled={clave.enCurso || nueva !== repetida}>
              Cambiar contraseña
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}

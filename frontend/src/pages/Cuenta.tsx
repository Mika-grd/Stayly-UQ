import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { sesion as servicioSesion } from '../api/servicios';
import type { Rol } from '../api/tipos';
import { useSesion } from '../auth/Sesion';
import { AvisoError, AvisoOk } from '../components/Comunes';
import { Fachada } from '../components/Fachada';
import { hoy } from '../lib/formato';
import { useAccion } from '../lib/useCarga';

const INICIO_POR_ROL: Record<Rol, string> = {
  HUESPED: '/viajes',
  RECEPCIONISTA: '/recepcion',
  ADMINISTRADOR: '/recepcion',
  PERSONAL_SERVICIO: '/operacion',
};

function Marco({ titulo, children }: { titulo: string; children: React.ReactNode }) {
  return (
    <main className="contenedor pagina cuenta">
      <div className="cuenta__ilustracion" aria-hidden="true">
        <Fachada identificacion="SUQ-202" dormitorios={2} />
      </div>
      <section className="placa placa--triple cuenta__formulario" style={{ '--losa': 'var(--cobalto)' } as React.CSSProperties}>
        <h1>{titulo}</h1>
        {children}
      </section>
    </main>
  );
}

export function Entrar() {
  const { iniciar } = useSesion();
  const navegar = useNavigate();
  const [params] = useSearchParams();
  const accion = useAccion();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const rol = await accion.ejecutar(() => iniciar(correo, contrasena));
    if (rol) navegar(params.get('volver') ?? INICIO_POR_ROL[rol], { replace: true });
  };

  return (
    <Marco titulo="Entrar">
      {params.get('registrado') && <AvisoOk>Cuenta creada. Ya puede entrar con su correo y contraseña.</AvisoOk>}
      <form className="pila" onSubmit={enviar}>
        <label className="campo">
          <span>Correo</span>
          <input type="email" required autoComplete="username" value={correo} onChange={(e) => setCorreo(e.target.value)} />
        </label>
        <label className="campo">
          <span>Contraseña</span>
          <input type="password" required autoComplete="current-password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} />
        </label>
        <AvisoError error={accion.error} />
        <button className="boton boton--principal boton--ancho" disabled={accion.enCurso}>
          {accion.enCurso ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <p>
        ¿Primera vez? <Link to={`/registro${params.get('volver') ? `?volver=${encodeURIComponent(params.get('volver')!)}` : ''}`}>Cree una cuenta</Link>
      </p>
    </Marco>
  );
}

export function Registro() {
  const navegar = useNavigate();
  const [params] = useSearchParams();
  const accion = useAccion();
  const [datos, setDatos] = useState({
    nombre: '',
    correo: '',
    contrasena: '',
    telefono: '',
    documentoIdentidad: '',
    fechaNacimiento: '',
  });
  const campo = (clave: keyof typeof datos) => ({
    value: datos[clave],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => setDatos((d) => ({ ...d, [clave]: e.target.value })),
  });

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const creado = await accion.ejecutar(() => servicioSesion.registrar(datos));
    if (creado) {
      const volver = params.get('volver');
      navegar(`/entrar?registrado=1${volver ? `&volver=${encodeURIComponent(volver)}` : ''}`);
    }
  };

  return (
    <Marco titulo="Crear cuenta de huésped">
      <form className="pila" onSubmit={enviar}>
        <label className="campo">
          <span>Nombre completo</span>
          <input required autoComplete="name" {...campo('nombre')} />
        </label>
        <label className="campo">
          <span>Correo</span>
          <input type="email" required autoComplete="email" {...campo('correo')} />
        </label>
        <label className="campo">
          <span>Contraseña</span>
          <input type="password" required minLength={8} autoComplete="new-password" {...campo('contrasena')} />
          <small>8+ caracteres, mayúscula y número</small>
        </label>
        <div className="fila">
          <label className="campo">
            <span>Documento</span>
            <input required inputMode="numeric" {...campo('documentoIdentidad')} />
          </label>
          <label className="campo">
            <span>Fecha de nacimiento</span>
            <input type="date" required max={hoy()} {...campo('fechaNacimiento')} />
          </label>
        </div>
        <label className="campo">
          <span>Teléfono</span>
          <input required autoComplete="tel" {...campo('telefono')} />
        </label>
        <AvisoError error={accion.error} />
        <button className="boton boton--principal boton--ancho" disabled={accion.enCurso}>
          {accion.enCurso ? 'Creando la cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p>
        ¿Ya tiene cuenta? <Link to="/entrar">Entrar</Link>
      </p>
    </Marco>
  );
}

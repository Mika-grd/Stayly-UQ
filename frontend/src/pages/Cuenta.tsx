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
      {params.get('recuperada') && <AvisoOk>Contraseña actualizada. Entre con la nueva.</AvisoOk>}
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
      <p className="nota">
        <Link to="/recuperar">Olvidé mi contraseña</Link>
      </p>
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

/** Recuperación de contraseña (7.2, CU-40): código al correo, 15 minutos, un solo uso. */
export function Recuperar() {
  const navegar = useNavigate();
  const solicitud = useAccion();
  const confirmacion = useAccion();
  const [correo, setCorreo] = useState('');
  const [enviado, setEnviado] = useState(false);
  const [codigo, setCodigo] = useState('');
  const [nueva, setNueva] = useState('');

  const pedir = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await solicitud.ejecutar(async () => {
      await servicioSesion.solicitarRecuperacion(correo);
      return true;
    });
    if (ok) setEnviado(true);
  };

  const confirmar = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await confirmacion.ejecutar(async () => {
      await servicioSesion.confirmarRecuperacion(correo, codigo.trim(), nueva);
      return true;
    });
    if (ok) navegar('/entrar?recuperada=1', { replace: true });
  };

  return (
    <Marco titulo="Recuperar contraseña">
      {!enviado ? (
        <form className="pila" onSubmit={pedir}>
          <label className="campo">
            <span>Correo de la cuenta</span>
            <input type="email" required autoComplete="username" value={correo} onChange={(e) => setCorreo(e.target.value)} />
          </label>
          <AvisoError error={solicitud.error} />
          <button className="boton boton--principal boton--ancho" disabled={solicitud.enCurso}>
            Enviar código
          </button>
        </form>
      ) : (
        <form className="pila" onSubmit={confirmar}>
          <AvisoOk>Si el correo está registrado, le llegó un código válido por 15 minutos.</AvisoOk>
          <label className="campo">
            <span>Código</span>
            <input required inputMode="numeric" autoComplete="one-time-code" value={codigo} onChange={(e) => setCodigo(e.target.value)} />
          </label>
          <label className="campo">
            <span>Nueva contraseña</span>
            <input
              type="password"
              required
              minLength={8}
              pattern="(?=.*[A-Z])(?=.*\d).{8,}"
              autoComplete="new-password"
              value={nueva}
              onChange={(e) => setNueva(e.target.value)}
            />
            <small>8+ caracteres, mayúscula y número</small>
          </label>
          <AvisoError error={confirmacion.error} />
          <button className="boton boton--principal boton--ancho" disabled={confirmacion.enCurso}>
            Cambiar contraseña
          </button>
          <button type="button" className="boton boton--texto" onClick={() => setEnviado(false)}>
            Usar otro correo
          </button>
        </form>
      )}
      <p>
        <Link to="/entrar">Volver a entrar</Link>
      </p>
    </Marco>
  );
}

import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useSesion } from '../auth/Sesion';
import { nombreRol } from '../lib/formato';
import { useTema } from '../lib/tema';

function Marca() {
  return (
    <Link to="/" className="marca" aria-label="Stayly, inicio">
      <svg width="30" height="30" viewBox="0 0 32 32" aria-hidden="true">
        <rect x="6" y="6" width="24" height="24" fill="var(--tinta)" />
        <rect x="2" y="2" width="24" height="24" fill="var(--balcon)" stroke="var(--tinta)" strokeWidth="2" />
        <rect x="10" y="12" width="8" height="14" fill="var(--maiz)" stroke="var(--tinta)" strokeWidth="2" />
      </svg>
      Stayly
    </Link>
  );
}

export function Cabecera() {
  const { rol, usuario, cerrar, es } = useSesion();
  const navegar = useNavigate();
  const { tema, alternar } = useTema();

  return (
    <header className="cabecera">
      <div className="contenedor cabecera__fila">
        <Marca />
        <nav className="nav" aria-label="Principal">
          <NavLink to="/" end>
            Explorar
          </NavLink>
          {es('HUESPED') && <NavLink to="/viajes">Mis viajes</NavLink>}
          {es('RECEPCIONISTA', 'ADMINISTRADOR') && <NavLink to="/recepcion">Recepción</NavLink>}
          {es('RECEPCIONISTA', 'ADMINISTRADOR', 'PERSONAL_SERVICIO') && <NavLink to="/operacion">Operación</NavLink>}
          {es('ADMINISTRADOR') && <NavLink to="/admin">Administración</NavLink>}
        </nav>
        <button type="button" className="tema" aria-pressed={tema === 'harsh'} onClick={alternar}>
          {tema === 'harsh' ? 'Normal' : 'Harsh'}
        </button>
        <div className="usuario">
          {rol ? (
            <>
              <Link to="/perfil" className="usuario__nombre">
                <div>{usuario?.nombre ?? 'Sesión iniciada'}</div>
                <div className="usuario__rol">{nombreRol[rol]}</div>
              </Link>
              <button
                className="boton boton--claro"
                onClick={() => {
                  cerrar();
                  navegar('/');
                }}
              >
                Salir
              </button>
            </>
          ) : (
            <>
              <Link to="/registro" className="boton boton--texto">
                Crear cuenta
              </Link>
              <Link to="/entrar" className="boton boton--maiz">
                Entrar
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export function Pie() {
  return (
    <footer className="pie">
      <div className="contenedor">
        <p>Stayly-UQ. Armenia, Quindío.</p>
      </div>
    </footer>
  );
}

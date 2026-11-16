import { NavLink, Navigate, useParams } from 'react-router-dom';
import { AdminAlojamiento } from './AdminAlojamiento';
import { AdminApartamentos } from './AdminApartamentos';
import { AdminCanales } from './AdminCanales';
import { AdminReportes } from './AdminReportes';
import { AdminTemporadas } from './AdminTemporadas';
import { AdminUsuarios } from './AdminUsuarios';

const SECCIONES = [
  { ruta: 'apartamentos', nombre: 'Apartamentos', pagina: AdminApartamentos },
  { ruta: 'temporadas', nombre: 'Temporadas y política', pagina: AdminTemporadas },
  { ruta: 'alojamiento', nombre: 'Alojamiento', pagina: AdminAlojamiento },
  { ruta: 'usuarios', nombre: 'Usuarios', pagina: AdminUsuarios },
  { ruta: 'canales', nombre: 'Canales', pagina: AdminCanales },
  { ruta: 'reportes', nombre: 'Reportes', pagina: AdminReportes },
] as const;

/** Área del administrador (7.1): una pestaña por recurso que gestiona. */
export function Administracion() {
  const { seccion } = useParams();
  const actual = SECCIONES.find((s) => s.ruta === seccion);
  if (!actual) return <Navigate to="/admin/apartamentos" replace />;
  const Pagina = actual.pagina;
  return (
    <main className="contenedor pagina">
      <h1>Administración</h1>
      <nav className="pestanas pestanas--enlaces" aria-label="Secciones de administración">
        {SECCIONES.map((s) => (
          <NavLink key={s.ruta} to={`/admin/${s.ruta}`}>
            {s.nombre}
          </NavLink>
        ))}
      </nav>
      <Pagina />
    </main>
  );
}

import { BrowserRouter, Link, Route, Routes, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { PERSONAL, ProveedorSesion, Requiere } from './auth/Sesion';
import { Cabecera, Pie } from './components/Cabecera';
import { Administracion } from './pages/admin/Administracion';
import { Apartamento } from './pages/Apartamento';
import { Entrar, Recuperar, Registro } from './pages/Cuenta';
import { Inicio } from './pages/Inicio';
import { MisViajes } from './pages/MisViajes';
import { ModificarReserva } from './pages/ModificarReserva';
import { Operacion } from './pages/Operacion';
import { Perfil } from './pages/Perfil';
import { Recepcion } from './pages/Recepcion';
import { Reservar } from './pages/Reservar';
import { ReservaDetalle } from './pages/ReservaDetalle';

function ArribaAlNavegar() {
  const { pathname } = useLocation();
  useEffect(() => {
    // En Chrome reciente scrollTo devuelve una promesa: no debe quedar como limpieza del efecto
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}

function NoEncontrada() {
  return (
    <main className="contenedor pagina">
      <h1>Esta página no existe</h1>
      <p>Revise la dirección o vuelva a buscar apartamentos.</p>
      <Link to="/" className="boton boton--principal">
        Buscar apartamentos
      </Link>
    </main>
  );
}

export function App() {
  return (
    <BrowserRouter>
      <ProveedorSesion>
        <ArribaAlNavegar />
        <Cabecera />
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/apartamentos/:id" element={<Apartamento />} />
          <Route path="/entrar" element={<Entrar />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/recuperar" element={<Recuperar />} />
          <Route
            path="/perfil"
            element={
              <Requiere>
                <Perfil />
              </Requiere>
            }
          />
          <Route
            path="/reservar/:id"
            element={
              <Requiere roles={['HUESPED', ...PERSONAL]}>
                <Reservar />
              </Requiere>
            }
          />
          <Route
            path="/viajes"
            element={
              <Requiere roles={['HUESPED']}>
                <MisViajes />
              </Requiere>
            }
          />
          <Route
            path="/reservas/:codigo"
            element={
              <Requiere>
                <ReservaDetalle />
              </Requiere>
            }
          />
          <Route
            path="/reservas/:codigo/modificar"
            element={
              <Requiere roles={PERSONAL}>
                <ModificarReserva />
              </Requiere>
            }
          />
          <Route
            path="/recepcion"
            element={
              <Requiere roles={PERSONAL}>
                <Recepcion />
              </Requiere>
            }
          />
          <Route
            path="/operacion"
            element={
              <Requiere roles={[...PERSONAL, 'PERSONAL_SERVICIO']}>
                <Operacion />
              </Requiere>
            }
          />
          <Route
            path="/admin/:seccion?"
            element={
              <Requiere roles={['ADMINISTRADOR']}>
                <Administracion />
              </Requiere>
            }
          />
          <Route path="*" element={<NoEncontrada />} />
        </Routes>
        <Pie />
      </ProveedorSesion>
    </BrowserRouter>
  );
}

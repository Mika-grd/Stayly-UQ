import { useCallback, useEffect, useState } from 'react';

/** Carga datos de la API al montar y cuando cambian las dependencias; expone recargar. */
export function useCarga<T>(cargar: () => Promise<T>, dependencias: unknown[]) {
  const [datos, setDatos] = useState<T | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [cargando, setCargando] = useState(true);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let vigente = true;
    setCargando(true);
    setError(null);
    cargar()
      .then((d) => vigente && setDatos(d))
      .catch((e) => vigente && setError(e))
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, version]);

  const recargar = useCallback(() => setVersion((v) => v + 1), []);
  return { datos, error, cargando, recargar, setDatos };
}

/** Ejecuta una acción de la API y guarda su error; devuelve true si salió bien. */
export function useAccion() {
  const [error, setError] = useState<unknown>(null);
  const [enCurso, setEnCurso] = useState(false);

  const ejecutar = useCallback(async <T,>(accion: () => Promise<T>): Promise<T | undefined> => {
    setEnCurso(true);
    setError(null);
    try {
      return await accion();
    } catch (e) {
      setError(e);
      return undefined;
    } finally {
      setEnCurso(false);
    }
  }, []);

  return { error, enCurso, ejecutar, limpiar: () => setError(null) };
}

import { useEffect, useState } from 'react';

export type Tema = 'normal' | 'harsh';
const CLAVE = 'stayly.tema';

function leer(): Tema {
  try {
    return localStorage.getItem(CLAVE) === 'harsh' ? 'harsh' : 'normal';
  } catch {
    return 'normal';
  }
}

/** Tema visual elegido; se guarda en el navegador y se aplica como data-tema en <html>. */
export function useTema() {
  const [tema, setTema] = useState<Tema>(leer);
  useEffect(() => {
    document.documentElement.dataset.tema = tema;
    try {
      localStorage.setItem(CLAVE, tema);
    } catch {
      /* sin almacenamiento: el tema dura lo que la pestaña */
    }
  }, [tema]);
  return { tema, alternar: () => setTema((t) => (t === 'harsh' ? 'normal' : 'harsh')) };
}
